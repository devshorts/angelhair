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
import org.apache.commons.lang.NotImplementedException;
import org.joda.time.DateTime;

import java.util.List;
import java.util.Optional;

@Data
class RepairContext{
    private final RepairBucketPointer pointer;

    private final Optional<DateTime> tombstonedAt;
}

public class RepairWorkerImpl implements RepairWorker {
    private final BucketConfiguration configuration;

    private final DataContext dataContext;

    @Inject
    public RepairWorkerImpl(
            BucketConfiguration configuration,
            DataContextFactory factory,
            @Assisted QueueName queueName) {
        this.configuration = configuration;
        dataContext = factory.forQueue(queueName);
    }

    @Override public void start() {

    }

    private RepairBucketPointer getCurrentBucket() {
        return dataContext.getPointerRepository().getRepairCurrentBucketPointer();
    }

    private void watchBucket(RepairContext pointer) {
        if(pointer.getTombstonedAt().isPresent()){
            waitForTimeout(pointer.getTombstonedAt().get());
        }

        final List<Message> messages = dataContext.getMessageRepository().getMessages(pointer.getPointer());

        messages.stream().filter(i -> !i.isAcked() && i.isVisible())
                .forEach(this::republishMessage);
    }

    private void waitForTimeout(final DateTime dateTime) {
        throw new NotImplementedException();
    }

    private RepairContext findFirstBucketToMonitor() {
        RepairBucketPointer currentBucket = getCurrentBucket();

        while (true) {
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

                else{
                    return new RepairContext(currentBucket, tombstoneTime);
                }
            }

            return new RepairContext(currentBucket, Optional.empty());
        }
    }

    private void republishMessage(Message message) {
        dataContext.getMessageRepository().putMessage(message);

        dataContext.getMessageRepository().ackMessage(message);
    }

    private RepairBucketPointer advance(final RepairBucketPointer currentBucket) {
        return dataContext.getPointerRepository().advanceRepairBucketPointer(currentBucket, currentBucket.next());
    }
}
