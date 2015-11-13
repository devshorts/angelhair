package com.godaddy.domains.cassandraqueue.workers;

import com.godaddy.domains.cassandraqueue.dataAccess.Tombstone;
import com.godaddy.domains.cassandraqueue.factories.DataContext;
import com.godaddy.domains.cassandraqueue.factories.DataContextFactory;
import com.godaddy.domains.cassandraqueue.model.InvisibilityMessagePointer;
import com.godaddy.domains.cassandraqueue.model.Message;
import com.godaddy.domains.cassandraqueue.model.MonotonicIndex;
import com.godaddy.domains.cassandraqueue.model.PopReceipt;
import com.godaddy.domains.cassandraqueue.model.ReaderBucketPointer;
import com.godaddy.logging.Logger;
import com.goddady.cassandra.queue.api.client.QueueName;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import org.joda.time.Duration;

import java.util.List;
import java.util.Optional;

import static com.godaddy.logging.LoggerFactory.getLogger;

public class ReaderImpl implements Reader {
    private static final Logger logger = getLogger(ReaderImpl.class);

    private final DataContext dataContext;
    private final BucketConfiguration config;

    @Inject
    public ReaderImpl(
            DataContextFactory dataContextFactory,
            BucketConfiguration config,
            @Assisted QueueName queueName) {
        this.config = config;
        dataContext = dataContextFactory.forQueue(queueName);
    }

    @Override
    public Optional<Message> nextMessage(Duration invisibility) {
        final Optional<Message> nowVisibleMessage = getNowVisibleMessage(getCurrentInvisPointer(), invisibility);

        if (nowVisibleMessage.isPresent()) {

            logger.with(nowVisibleMessage.get()).info("Got newly visible message");

            return nowVisibleMessage;
        }

        final Optional<Message> nextMessage = getAndMark(getReaderCurrentBucket(), invisibility);

        if (nextMessage.isPresent()) {
            logger.with(nextMessage.get()).info("Got message");
        }

        return nextMessage;
    }

    @Override
    public boolean ackMessage(final PopReceipt popReceipt) {
        final Message messageAt = dataContext.getMessageRepository().getMessage(popReceipt.getMessageIndex());

        if (messageAt.getVersion() != popReceipt.getMessageVersion()) {
            return false;
        }

        return dataContext.getMessageRepository().ackMessage(messageAt);
    }

    private InvisibilityMessagePointer getCurrentInvisPointer() {
        return dataContext.getPointerRepository().getCurrentInvisPointer();
    }

    private ReaderBucketPointer getReaderCurrentBucket() {
        return dataContext.getPointerRepository().getReaderCurrentBucket();
    }

    private Optional<Message> getNowVisibleMessage(InvisibilityMessagePointer pointer, Duration invisiblity) {
        final Message messageAt = dataContext.getMessageRepository().getMessage(pointer);

        if(messageAt == null){
            return setNextInvisiblityPointer(pointer, invisiblity);
        }

        if (messageAt.getDeliveryCount() == 0) {
            return Optional.empty();
        }

        if (messageAt.isVisible() && messageAt.isNotAcked()) {
            return dataContext.getMessageRepository().consumeNewlyVisibleMessage(messageAt, invisiblity);
        }
        else if (messageAt.isAcked()) {
            return setNextInvisiblityPointer(pointer, invisiblity);
        }

        return Optional.empty();
    }

    private Optional<Message> setNextInvisiblityPointer(final InvisibilityMessagePointer pointer, Duration invisiblity) {
        final ReaderBucketPointer bucketPointer = pointer.toBucketPointer(config.getBucketSize());

        final List<Message> messages = dataContext.getMessageRepository().getMessages(bucketPointer);

        if(messages.isEmpty() || messages.stream().allMatch(m -> m.getIndex() == Tombstone.index)){
            return Optional.empty();
        }

        final Optional<Message> first = messages.stream()
                                                .filter(m -> m.isNotAcked() &&
                                                             m.isNotVisible() &&
                                                             m.getDeliveryCount() > 0).findFirst();

        if (first.isPresent()) {
            dataContext.getPointerRepository().moveInvisiblityPointerTo(pointer, InvisibilityMessagePointer.valueOf(first.get().getIndex()));

            logger.with(first.get()).info("Found invis message in current bucket");

            return Optional.empty();
        }

        logger.with(pointer).info("Moving invis pointer to next bucket");

        final MonotonicIndex monotonicIndex = bucketPointer.next().startOf(config.getBucketSize());

        return getNowVisibleMessage(InvisibilityMessagePointer.valueOf(monotonicIndex), invisiblity);
    }

    private Optional<Message> getAndMark(ReaderBucketPointer currentBucket, Duration invisiblity) {

        final List<Message> allMessages = dataContext.getMessageRepository().getMessages(currentBucket);

        final boolean allComplete = allMessages.stream().allMatch(Message::isAcked);

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

        if (!dataContext.getMessageRepository().consumeMessage(message, invisiblity)) {
            // someone else did it, fuck it, try again for the next message
            logger.with(message).warn("Someone else consumed the message!");

            return getAndMark(currentBucket, invisiblity);
        }

        return Optional.of(message);
    }

    private void tombstone(final ReaderBucketPointer bucket) {
        logger.with(bucket).info("Tombstoning reader");
        dataContext.getMessageRepository().tombstone(bucket);
    }

    private boolean monotonPastBucket(final ReaderBucketPointer currentBucket) {
        final ReaderBucketPointer currentMonotonicBucket = getLatestMonotonic().toBucketPointer(config.getBucketSize());

        return currentMonotonicBucket.get() > currentBucket.get();
    }

    private ReaderBucketPointer advanceBucket(ReaderBucketPointer currentBucket) {
        logger.with(currentBucket).info("Advancing reader bucket");

        return dataContext.getPointerRepository().advanceMessageBucketPointer(currentBucket, currentBucket.next());
    }

    private MonotonicIndex getLatestMonotonic() {
        return dataContext.getMonotonicRepository().getCurrent();
    }
}
