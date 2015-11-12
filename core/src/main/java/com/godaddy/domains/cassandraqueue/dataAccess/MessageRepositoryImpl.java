package com.godaddy.domains.cassandraqueue.dataAccess;

import com.datastax.driver.core.Session;
import com.godaddy.domains.cassandraqueue.dataAccess.interfaces.MessageRepository;
import com.godaddy.domains.cassandraqueue.model.BucketPointer;
import com.godaddy.domains.cassandraqueue.model.Message;
import com.godaddy.domains.cassandraqueue.model.MessagePointer;
import com.godaddy.domains.cassandraqueue.model.MonotonicIndex;
import com.godaddy.domains.cassandraqueue.model.QueueName;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import org.apache.commons.lang.NotImplementedException;
import org.joda.time.Duration;

import java.util.List;

public class MessageRepositoryImpl implements MessageRepository {
    private final Session session;
    private final QueueName queueName;

    @Inject
    public MessageRepositoryImpl(Session session, @Assisted QueueName queueName) {
        this.session = session;
        this.queueName = queueName;
    }

    @Override public void putMessage(final Message message, Duration initialInvisibility) {
        // store message
        throw new NotImplementedException();
    }

    @Override public boolean markMessageInvisible(final Message message, final Duration duration) {
        // update message invisiblity value to utc now + duration
        // conditionally update index to use invisiblity if version the same
        throw new NotImplementedException();
    }

    @Override public void ackMessage(final MonotonicIndex messageId) {
        // mark message as consumed
        throw new NotImplementedException();
    }

    @Override public List<Message> getMessages(final BucketPointer bucketPointer) {
        // list all messages in bucket
        throw new NotImplementedException();
    }

    @Override public void tombstone(final BucketPointer bucketPointer) {
        // mark the bucket as tombstoned
        throw new NotImplementedException();
    }

    @Override public Message getMessageAt(final MessagePointer pointer) {
        throw new NotImplementedException();
    }
}
