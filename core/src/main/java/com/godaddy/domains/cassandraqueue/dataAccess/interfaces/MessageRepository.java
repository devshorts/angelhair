package com.godaddy.domains.cassandraqueue.dataAccess.interfaces;

import com.godaddy.domains.cassandraqueue.model.BucketPointer;
import com.godaddy.domains.cassandraqueue.model.Message;
import com.godaddy.domains.cassandraqueue.model.MessagePointer;
import com.godaddy.domains.cassandraqueue.model.MonotonicIndex;
import org.joda.time.Duration;

import java.util.List;

public interface MessageRepository {
    void putMessage(Message message, Duration initialInvisibility);

    void markMessageInvisible(MonotonicIndex message, Duration duration);

    void ackMessage(MonotonicIndex messageId);

    List<Message> getMessages(BucketPointer bucketPointer);

    void tombstone(BucketPointer bucketPointer);

    Message getMessageAt(MessagePointer pointer);
}
