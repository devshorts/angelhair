package com.godaddy.domains.cassandraqueue.workers;

import com.godaddy.domains.cassandraqueue.factories.DataContext;
import com.godaddy.domains.cassandraqueue.factories.DataContextFactory;
import com.godaddy.domains.cassandraqueue.model.BucketPointer;
import com.godaddy.domains.cassandraqueue.model.InvisibilityMessagePointer;
import com.godaddy.domains.cassandraqueue.model.Message;
import com.godaddy.domains.cassandraqueue.model.MonotonicIndex;
import com.godaddy.domains.cassandraqueue.model.PopReceipt;
import com.godaddy.domains.cassandraqueue.model.QueueName;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import org.apache.commons.lang.NotImplementedException;
import org.joda.time.Duration;

import java.util.List;
import java.util.Optional;

public class ReaderImpl implements Reader {
    private final DataContext dataContext;
    private final DataContextFactory dataContextFactory;
    private final BucketConfiguration config;
    private final QueueName queueName;

    @Inject
    public ReaderImpl(
            DataContextFactory dataContextFactory,
            BucketConfiguration config,
            @Assisted QueueName queueName) {
        this.dataContextFactory = dataContextFactory;
        this.config = config;
        this.queueName = queueName;
        dataContext = dataContextFactory.forQueue(queueName);
    }

    @Override public Optional<Message> nextMessage(Duration invisibility) {
        final Optional<Message> nowVisibleMessage = getNowVisibleMessage(getCurrentInvisPointer(), invisibility);

        if (nowVisibleMessage.isPresent()) {
            return nowVisibleMessage;
        }

        return getAndMark(getReaderCurrentBucket(), invisibility);
    }

    private InvisibilityMessagePointer getCurrentInvisPointer() {
        return dataContext.getPointerRepository().getCurrentInvisPointer();
    }

    @Override public void ackMessage(final PopReceipt popReceipt) {
        throw new NotImplementedException();
    }

    private BucketPointer getReaderCurrentBucket() {
        return dataContext.getPointerRepository().getReaderCurrentBucket();
    }

    private Optional<Message> getNowVisibleMessage(InvisibilityMessagePointer pointer, Duration invisiblity) {
        final Message messageAt = dataContext.getMessageRepository().getMessageAt(pointer);

        if (messageAt.isVisible() && !messageAt.isNotAcked()) {
            if (updateInivisiblityTime(messageAt, invisiblity)) {
                return Optional.of(messageAt);
            }
        }
        else if (messageAt.isAcked()) {
            return setNextInvisiblityPointer(pointer, invisiblity);
        }

        return Optional.empty();
    }

    private Optional<Message> setNextInvisiblityPointer(final InvisibilityMessagePointer pointer, Duration invisiblity) {
        final BucketPointer bucketPointer = pointer.toBucketPointer(config.getBucketSize());

        final List<Message> messages = dataContext.getMessageRepository().getMessages(bucketPointer);

        final Optional<Message> first = messages.stream().filter(m -> m.isNotAcked() && m.isNotVisible()).findFirst();

        if (first.isPresent()) {
            dataContext.getPointerRepository().moveInvisiblityPointerTo(first.get().getIndex());

            return Optional.empty();
        }

        final MonotonicIndex monotonicIndex = bucketPointer.next().startOf(config.getBucketSize());

        return getNowVisibleMessage(InvisibilityMessagePointer.valueOf(monotonicIndex), invisiblity);
    }

    private boolean updateInivisiblityTime(final Message message, final Duration invisiblity) {
        return dataContext.getMessageRepository().markMessageInvisible(message, invisiblity);
    }

    private Optional<Message> getAndMark(BucketPointer currentBucket, Duration invisiblity) {

        final List<Message> allMessages = dataContext.getMessageRepository().getMessages(currentBucket);

        final boolean allComplete = allMessages.stream().allMatch(Message::isNotVisible);

        if (allComplete) {
            if (allMessages.size() == config.getBucketSize() || monotonPastBucket(currentBucket)) {
                tombstone(currentBucket);

                return getAndMark(advanceBucket(currentBucket), invisiblity);
            }
            else {
                // bucket not ready to be closed yet, but all current messages processed
                return Optional.empty();
            }
        }

        final Optional<Message> foundMessage = allMessages.stream().filter(Message::isVisible).findFirst();

        if (!foundMessage.isPresent()) {
            return Optional.empty();
        }

        final Message message = foundMessage.get();

        if (!updateInivisiblityTime(message, invisiblity)) {
            // someone else did it, fuck it, try again for the next message
            return getAndMark(currentBucket, invisiblity);
        }

        return Optional.of(message);
    }

    private void tombstone(final BucketPointer bucket) {
        dataContext.getMessageRepository().tombstone(bucket);
    }

    private boolean monotonPastBucket(final BucketPointer currentBucket) {
        final BucketPointer currentMonotonicBucket = getLatestMonotonic().toBucketPointer(config.getBucketSize());

        return currentMonotonicBucket.get() > currentBucket.get();
    }

    private BucketPointer advanceBucket(BucketPointer currentBucket) {
        return dataContext.getPointerRepository().advanceMessageBucketPointer(currentBucket);
    }

    private MonotonicIndex getLatestMonotonic() {
        return dataContext.getMonotonicRepository().getCurrent();
    }
}
