package com.godaddy.domains.cassandraqueue.workers;

import com.godaddy.domains.cassandraqueue.factories.DataContext;
import com.godaddy.domains.cassandraqueue.factories.DataContextFactory;
import com.godaddy.domains.cassandraqueue.model.BucketPointer;
import com.godaddy.domains.cassandraqueue.model.InvisibilityMessagePointer;
import com.godaddy.domains.cassandraqueue.model.Message;
import com.godaddy.domains.cassandraqueue.model.MessageId;
import com.godaddy.domains.cassandraqueue.model.MonotonicIndex;
import com.godaddy.domains.cassandraqueue.model.QueueName;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import org.joda.time.Duration;

import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.toList;

public class ReaderImpl implements Reader {
    private final DataContext dataContext;

    @Inject
    public ReaderImpl(DataContextFactory dataContextFactory, @Assisted QueueName queueName) {
        dataContext = dataContextFactory.forQueue(queueName);
    }

    @Override public Optional<Message> nextMessage(Duration invisibility) {
        final Optional<Message> nowVisibleMessage = getNowVisibleMessage(invisibility);

        if (nowVisibleMessage.isPresent()) {
            return nowVisibleMessage;
        }

        return getAndMark(getCurrentBucket(), invisibility);
    }

    @Override public void ackMessage(final MessageId messageId) {

    }

    private BucketPointer getCurrentBucket() {
        return null;
    }

    private Optional<Message> getNowVisibleMessage(InvisibilityMessagePointer pointer, Duration invisiblity) {
        final Message messageAt = dataContext.getMessageRepository().getMessageAt(pointer);

        if (messageAt.isVisible()) {
            if (updateInivisiblityTime(messageAt, invisiblity)) {
                return Optional.of(messageAt);
            }
        }

        return Optional.empty();
    }

    private boolean updateInivisiblityTime(final Message message, final Duration invisiblity) {
        // conditionally update index to use invisiblity if version the same

        return false;
    }

    private Optional<Message> getAndMark(BucketPointer currentBucket, Duration invisiblity) {

        final List<Message> collect =
                dataContext.getMessageRepository()
                           .getMessages(currentBucket)
                           .stream()
                           .filter(Message::isVisible)
                           .collect(toList());


        /*
            if an unread visible message exists
                make invisible for duration
                update invisiblity pointer IFF the message monotonic is less than the existing invisiblity pointer
         */

        return Optional.empty();
    }

    private void closeCompleteBucket(BucketPointer currentBucket) {
        /*
             all the messages in the bucket are marked as invisible or consumed
         */
    }

    private BucketPointer getNextBucket(BucketPointer currentBucket) {
        return null;
    }

    private boolean bucketHasUnreadMessages(BucketPointer bucketPointer) {
        return false;
    }

    private BucketPointer getLastBucket(MonotonicIndex maxMonoton) {
        return null;
    }

    private MonotonicIndex getLastMonotonic() {
        return null;
    }

    private Optional<MonotonicIndex> jumpMissingBucket(BucketPointer currentBucket) {
        /*
            If all the messages in the bucket are invisible and marked but the size isn't the size of the bucket
                get the next monotic value
                determine the bucket that monotic is in
                see if a message can be found in the next bucket
         */

        closeCompleteBucket(currentBucket);

        final MonotonicIndex lastMonotonic = getLastMonotonic();

        while (bucketHasUnreadMessages(currentBucket) && currentBucket.get() < getLastBucket(lastMonotonic).get()) {
            final Optional<MonotonicIndex> andMark = getAndMark(currentBucket);

            if (andMark.isPresent()) {
                return andMark;
            }

            currentBucket = currentBucket.next();
        }

        return Optional.empty();
    }
}
