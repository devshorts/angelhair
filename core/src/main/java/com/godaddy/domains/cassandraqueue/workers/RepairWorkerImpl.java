package com.godaddy.domains.cassandraqueue.workers;

import com.godaddy.domains.cassandraqueue.ServiceConfiguration;
import com.godaddy.domains.cassandraqueue.dataAccess.interfaces.MessageRepository;
import com.godaddy.domains.cassandraqueue.factories.DataContext;
import com.godaddy.domains.cassandraqueue.factories.DataContextFactory;
import com.godaddy.domains.cassandraqueue.model.Clock;
import com.godaddy.domains.cassandraqueue.model.Message;
import com.godaddy.domains.cassandraqueue.model.MonotonicIndex;
import com.godaddy.domains.cassandraqueue.model.QueueDefinition;
import com.godaddy.domains.cassandraqueue.model.RepairBucketPointer;
import com.godaddy.domains.cassandraqueue.modules.annotations.RepairPool;
import com.godaddy.logging.Logger;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import lombok.Data;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Instant;
import org.joda.time.Seconds;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static com.godaddy.logging.LoggerFactory.getLogger;

@Data
class RepairContext {
    private final RepairBucketPointer pointer;

    private final DateTime tombstonedAt;
}

public class RepairWorkerImpl implements RepairWorker {
    private final BucketConfiguration configuration;
    private final Clock clock;
    private final ScheduledExecutorService scheduledExecutorService;
    private final QueueDefinition queueDefinition;

    private Logger logger = getLogger(RepairWorkerImpl.class);

    private final DataContext dataContext;

    private volatile boolean isStarted;

    private final Object nextRun = new Object();

    @Inject
    public RepairWorkerImpl(
            ServiceConfiguration configuration,
            DataContextFactory factory,
            Clock clock,
            @RepairPool ScheduledExecutorService executorService,
            @Assisted QueueDefinition definition) {
        this.clock = clock;
        scheduledExecutorService = executorService;
        queueDefinition = definition;
        this.configuration = configuration.getBucketConfiguration();
        dataContext = factory.forQueue(definition);

        logger = logger.with(definition.getQueueName());
    }

    @Override
    public void start() {
        isStarted = true;

        logger.success("Starting repairer");

        schedule();
    }

    @Override
    public void stop() {
        isStarted = false;

        scheduledExecutorService.shutdown();
    }

    public void waitForNextRun() throws InterruptedException {
        synchronized (nextRun) {
            nextRun.wait();
        }
    }

    private void schedule() {
        scheduledExecutorService.schedule(this::process,
                                          configuration.getRepairWorkerPollFrequency().getMillis(), TimeUnit.MILLISECONDS);
    }

    private void process() {
        try {
            final Optional<RepairContext> firstBucketToMonitor = findFirstBucketToMonitor();

            if (firstBucketToMonitor.isPresent()) {
                watchBucket(firstBucketToMonitor.get());
                synchronized (nextRun) {
                    nextRun.notifyAll();
                }
            }
        }
        catch (Throwable ex) {
            logger.error(ex, "Error processing!");
        }
        finally {
            schedule();
        }
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

        messages.stream().filter(message -> !message.isAcked() && message.isVisible(clock) && message.getDeliveryCount() == 0)
                .forEach(this::republishMessage);

        advance(pointer.getPointer());
    }

    private void waitForTimeout(final DateTime tombstoneTime) {

        final DateTime plus = tombstoneTime.plus(configuration.getRepairWorkerTimeout());

        final Instant now = clock.now();

        final Seconds seconds = Seconds.secondsBetween(now, plus);

        logger.with("tombstone-time", tombstoneTime)
              .with("now", now)
              .with("seconds-to-wait", seconds)
              .debug("Need to wait for bucket to be time closed");

        if (seconds.isGreaterThan(Seconds.ZERO)) {
            // wait for the repair worker timeout
            try {
                clock.sleepFor(seconds.toStandardDuration());
            }
            catch (InterruptedException e) {
                // ok
            }
        }

        logger.success("Bucket should be closed");
    }

    private Optional<RepairContext> findFirstBucketToMonitor() {
        RepairBucketPointer currentBucket = getCurrentBucket();

        while (isStarted) {
            // first bucket that is tombstoned and is unfilled
            final MessageRepository messageRepository = dataContext.getMessageRepository();

            final Optional<DateTime> tombstoneTime = messageRepository.tombstoneExists(currentBucket);

            if (tombstoneTime.isPresent()) {
                final List<Message> messages = messageRepository.getMessages(currentBucket);

                if (messages.size() == queueDefinition.getBucketSize() && messages.stream().allMatch(Message::isAcked)) {
                    currentBucket = advance(currentBucket);

                    logger.with(currentBucket).info("Found full bucket, advancing");

                    // look for next bucket
                    continue;
                }

                else {
                    logger.with(currentBucket).info("Found tombstoned bucket, going to watch");

                    // found a bucket that is tombestoned, and need to now wait for the timeout
                    // before processing all messages and moving on
                    return Optional.of(new RepairContext(currentBucket, tombstoneTime.get()));
                }
            }

            logger.with(currentBucket).debug("On active bucket not tombstoned");

            // on an active bucket that isn't tombstoned, just come back later and wait for tombstone
            return Optional.empty();
        }

        return Optional.empty();
    }

    private void republishMessage(Message message) {
        try {
            final MonotonicIndex nextIndex = dataContext.getMonotonicRepository().nextMonotonic();

            dataContext.getMessageRepository().putMessage(message.createNewWithIndex(nextIndex));

            dataContext.getMessageRepository().ackMessage(message);

            logger.with(message).with("next-index", nextIndex)
                  .info("Message needs republishing, acking original and publishing new one");
        }
        catch (Exception e) {
            logger.error(e, "Error publishing message");

            throw new RuntimeException(e);
        }
    }

    private RepairBucketPointer advance(final RepairBucketPointer currentBucket) {
        logger.info("Advancing bucket");

        final RepairBucketPointer repairBucketPointer = dataContext.getPointerRepository().advanceRepairBucketPointer(currentBucket, currentBucket.next());

        logger.with(repairBucketPointer).info("New bucket");

        return repairBucketPointer;
    }
}
