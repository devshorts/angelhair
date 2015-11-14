package com.godaddy.domains.cassandraqueue.workers;

import com.godaddy.domains.cassandraqueue.factories.DataContext;
import com.godaddy.domains.cassandraqueue.factories.DataContextFactory;
import com.godaddy.domains.cassandraqueue.model.InvisibilityMessagePointer;
import com.godaddy.domains.cassandraqueue.model.Message;
import com.godaddy.domains.cassandraqueue.model.MessagePointer;
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

/**
 * Invis pointer algo:
 *
 * if a message is available for consumption (never consumed)
 *
 * Story time!
 *
 * Imagine this scenario:
 *
 * ~   = out for consumption
 * INV = message is invisible
 * = location of inivs pointer
 * T   = tombstoned
 * +   = at least once delivered
 * A   = acked
 * --  = bucket line
 *
 * Message Id | Status
 *
 * 0 A *
 * 1
 * 2
 * --
 * 3
 *
 * Zero is acked. 1, 2 and 3. Two reads come in at the same time.  Both try and claim 1,
 * but only 1 of the consumers gets in, so the failed consumer (due to version changes)
 * retries and gets message 2.  Invis pointer is still on zero, since it can't move past
 * never delivered messages and is only moved on read begin.
 *
 * 0 A *
 * 1 ~ INV - DEAD
 * 2 ~ INV
 * --
 * 3
 *
 * Lets say now that message 2 is acked
 *
 * 0 A *
 * 1 ~ INV - DEAD
 * 2 A
 * --
 * 3
 *
 * Now two more reads come in and message 1 is ready for redelivery since its alive again
 *
 * At this point, the invis pointer finds message 1 and sits on it. It gets returned as the message to consume
 * since its alive again, its visiblity gets updated to next, and the invis pointer parks.
 *
 * 0 A
 * 1 + *
 * 2 A
 * --
 * 3
 *
 * Now message 1 is acked, invis pointer stays put. The next read comes in, invis pointer moves to 3
 * and parks since its not allowed to advance past never delivered messages
 *
 * 0 A
 * 1 A
 * 2 A
 * --
 * 3 *
 */
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
        final Optional<Message> nowVisibleMessage = tryGetNowVisibleMessage(getCurrentInvisPointer(), invisibility);

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

    private Optional<Message> tryGetNowVisibleMessage(InvisibilityMessagePointer pointer, Duration invisiblity) {
        final Message messageAt = dataContext.getMessageRepository().getMessage(pointer);

        if (messageAt == null) {
            // invis pointer points to garbage, try and find something else
            return tryGetNextInvisMessage(pointer, invisiblity);
        }

        if (messageAt.getDeliveryCount() == 0) {
            // it hasn't been sent out for delivery yet so can't be invisible
            return Optional.empty();
        }

        if (messageAt.isVisible() && messageAt.isNotAcked()) {
            // the message has come back alive
            return dataContext.getMessageRepository().consumeMessage(messageAt, invisiblity);
        }
        else if (messageAt.isAcked()) {
            // current message is acked that the invis pointer was pointing to
            // try and move the invis pointer to the next lowest monotonic invisible
            return tryGetNextInvisMessage(pointer, invisiblity);
        }

        return Optional.empty();
    }

    private Optional<Message> tryGetNextInvisMessage(final InvisibilityMessagePointer pointer, Duration invisiblity) {
        // check all the messages in the bucket the invis pointer is currently on
        final ReaderBucketPointer bucketPointer = pointer.toBucketPointer(config.getBucketSize());

        final List<Message> messages = dataContext.getMessageRepository().getMessages(bucketPointer);

        if (messages.isEmpty()) {
            // no messages, can't move pointer since nothing to move to
            return Optional.empty();
        }

        // in the active bucket, if there is a not acked, not visible, at least once delivered message
        // then if its ID is LESS than the current active pointer, move the pointer to that
        // otherwise pointer stays the same
        final Optional<Message> first = messages.stream()
                                                .filter(m -> m.isNotAcked() &&
                                                             m.isNotVisible() &&
                                                             m.getDeliveryCount() > 0).findFirst();

        if (first.isPresent()) {
            trySetNewInvisPointer(pointer, first.get().getIndex());

            logger.with(first.get()).info("Found invis message in current bucket");

            return Optional.empty();
        }

        final InvisibilityMessagePointer pointerForNextBucket = getPointerForNextBucket(pointer);

        return tryGetNowVisibleMessage(pointerForNextBucket, invisiblity);
    }

    /**
     * Given the current pointer, returns a new pointer that jumps to the start of the next bucket
     *
     * @param pointer
     * @return
     */
    private InvisibilityMessagePointer getPointerForNextBucket(InvisibilityMessagePointer pointer) {
        final ReaderBucketPointer bucketPointer = pointer.toBucketPointer(config.getBucketSize());

        final MonotonicIndex monotonicIndex = bucketPointer.next().startOf(config.getBucketSize());

        return InvisibilityMessagePointer.valueOf(monotonicIndex);
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

        final Message visibleMessage = foundMessage.get();

        final Optional<Message> consumedMessage = dataContext.getMessageRepository().consumeMessage(visibleMessage, invisiblity);

        if (!consumedMessage.isPresent()) {
            // someone else did it, fuck it, try again for the next visibleMessage
            logger.with(visibleMessage).warn("Someone else consumed the visibleMessage!");

            return getAndMark(currentBucket, invisiblity);
        }

        return consumedMessage;
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

    private void trySetNewInvisPointer(final InvisibilityMessagePointer currentInvis, MessagePointer potentialNextInvisPointer) {
        dataContext.getPointerRepository().tryMoveInvisiblityPointerTo(currentInvis, InvisibilityMessagePointer.valueOf(potentialNextInvisPointer.get()));
    }
}
