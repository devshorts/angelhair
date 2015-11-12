package com.godaddy.domains.cassandraqueue.workers;

import com.godaddy.domains.cassandraqueue.dataAccess.interfaces.MessageRepository;
import com.godaddy.domains.cassandraqueue.factories.DataContext;
import com.godaddy.domains.cassandraqueue.factories.DataContextFactory;
import com.godaddy.domains.cassandraqueue.model.Message;
import com.godaddy.domains.cassandraqueue.model.QueueName;
import com.godaddy.domains.cassandraqueue.model.RepairBucketPointer;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import lombok.Data;
import org.joda.time.DateTime;
import org.joda.time.Period;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Data class RepairContext {
    private final RepairBucketPointer pointer;

    private final DateTime tombstonedAt;
}

public class RepairWorkerImpl implements RepairWorker {
    private final BucketConfiguration configuration;

    private final DataContext dataContext;

    private volatile boolean isStarted;

    private final ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);

    @Inject
    public RepairWorkerImpl(
            BucketConfiguration configuration,
            DataContextFactory factory,
            @Assisted QueueName queueName) {
        this.configuration = configuration;
        dataContext = factory.forQueue(queueName);
    }

    @Override public void start() {
        isStarted = true;

        schedule();
    }

    private void schedule() {
        scheduledExecutorService.schedule(this::process,
                                          configuration.getRepairWorkerPollFrequency().getMillis(), TimeUnit.MILLISECONDS);
    }

    @Override public void stop() {
        isStarted = false;

        scheduledExecutorService.shutdown();
    }

    private void process() {
        final Optional<RepairContext> firstBucketToMonitor = findFirstBucketToMonitor();

        if (firstBucketToMonitor.isPresent()) {
            watchBucket(firstBucketToMonitor.get());
        }

        schedule();
    }

    private RepairBucketPointer getCurrentBucket() {
        return dataContext.getPointerRepository().getRepairCurrentBucketPointer();
    }

    private void watchBucket(RepairContext pointer) {
        waitForTimeout(pointer.getTombstonedAt());

        if (!isStarted) {
            return;
        }

        final List<Message> messages = dataContext.getMessageRepository().getMessages(pointer.getPointer());

        messages.stream().filter(i -> !i.isAcked() && i.isVisible())
                .forEach(this::republishMessage);
    }

    private void waitForTimeout(final DateTime tombstoneTime) {
        final Period period = new Period(tombstoneTime.withDurationAdded(configuration.getRepairWorkerTimeout(), 1), DateTime.now());

        if (period.getMillis() > 0) {
            // wait for the repair worker timeout
            try {
                Thread.sleep(period.getMillis());
            }
            catch (InterruptedException e) {
                // ok
            }
        }
    }

    private Optional<RepairContext> findFirstBucketToMonitor() {
        RepairBucketPointer currentBucket = getCurrentBucket();

        while (isStarted) {
            // first bucket that is tombstoned and is unfilled
            final MessageRepository messageRepository = dataContext.getMessageRepository();

            final Optional<DateTime> tombstoneTime = messageRepository.tombstoneExists(currentBucket);

            if (tombstoneTime.isPresent()) {
                final List<Message> messages = messageRepository.getMessages(currentBucket);

                if (messages.size() == configuration.getBucketSize() && messages.stream().allMatch(Message::isAcked)) {
                    currentBucket = advance(currentBucket);

                    // look for next bucket
                    continue;
                }

                else {
                    // found a bucket that is tombestoned, and need to now wait for the timeout
                    // before processing all messages and moving on
                    return Optional.of(new RepairContext(currentBucket, tombstoneTime.get()));
                }
            }

            // on an active bucket that isn't tombstoned, just come back later and wait for tombstone
            return Optional.empty();
        }

        return Optional.empty();
    }

    private void republishMessage(Message message) {
        dataContext.getMessageRepository().putMessage(message);

        dataContext.getMessageRepository().ackMessage(message);
    }

    private RepairBucketPointer advance(final RepairBucketPointer currentBucket) {
        return dataContext.getPointerRepository().advanceRepairBucketPointer(currentBucket, currentBucket.next());
    }
}
