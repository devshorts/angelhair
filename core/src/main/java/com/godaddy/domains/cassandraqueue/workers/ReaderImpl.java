package com.godaddy.domains.cassandraqueue.workers;

import com.godaddy.domains.cassandraqueue.factories.DataContext;
import com.godaddy.domains.cassandraqueue.factories.DataContextFactory;
import com.godaddy.domains.cassandraqueue.model.BucketPointer;
import com.godaddy.domains.cassandraqueue.model.Message;
import com.godaddy.domains.cassandraqueue.model.MessageId;
import com.godaddy.domains.cassandraqueue.model.MonotonicIndex;
import com.godaddy.domains.cassandraqueue.model.QueueName;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

import java.util.Optional;

public class ReaderImpl implements Reader {
    private final DataContext dataContext;

    @Inject
    public ReaderImpl(DataContextFactory dataContextFactory, @Assisted QueueName queueName) {
        dataContext = dataContextFactory.forQueue(queueName);
    }

    @Override public Message nextMessage() {
        return null;
    }

    @Override public void ackMessage(final MessageId messageId) {

    }

    private BucketPointer getCurrentBucket(){
        return null;
    }

    private Optional<MonotonicIndex> getAndMark(BucketPointer currentBucket){
        /*
            if an unread visible message exists
                make invisible for duration
                update invisiblity pointer IFF the message monotonic is less than the existing invisiblity pointer
         */

        return Optional.empty();
    }

    private void closeCompleteBucket(BucketPointer currentBucket){
        /*
             all the messages in the bucket are marked as invisible or consumed
         */
    }

    private BucketPointer getNextBucket(BucketPointer currentBucket){
        return null;
    }

    private boolean bucketHasUnreadMessages(BucketPointer bucketPointer){
        return false;
    }

    private BucketPointer getLastBucket(MonotonicIndex maxMonoton){
        return null;
    }

    private MonotonicIndex getLastMonotonic(){
        return null;
    }

    private Optional<MonotonicIndex> jumpMissingBucket(BucketPointer currentBucket){
        /*
            If all the messages in the bucket are invisible and marked but the size isn't the size of the bucket
                get the next monotic value
                determine the bucket that monotic is in
                see if a message can be found in the next bucket
         */

        closeCompleteBucket(currentBucket);

        final MonotonicIndex lastMonotonic = getLastMonotonic();

        while(bucketHasUnreadMessages(currentBucket) && currentBucket.get() < getLastBucket(lastMonotonic).get()){
            final Optional<MonotonicIndex> andMark = getAndMark(currentBucket);

            if(andMark.isPresent()){
                return andMark;
            }

            currentBucket = currentBucket.next();
        }

        return Optional.empty();
    }
}
