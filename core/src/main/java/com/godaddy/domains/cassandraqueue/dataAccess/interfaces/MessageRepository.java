package com.godaddy.domains.cassandraqueue.dataAccess.interfaces;

import com.godaddy.domains.cassandraqueue.model.ReaderBucketPointer;
import com.godaddy.domains.cassandraqueue.model.Message;
import com.godaddy.domains.cassandraqueue.model.MessagePointer;
import org.joda.time.Duration;

import java.util.List;

public interface MessageRepository {
    void putMessage(Message message, Duration initialInvisibility);

    boolean markMessageInvisible(Message message, Duration duration);

    boolean ackMessage(Message message);

    List<Message> getMessages(ReaderBucketPointer bucketPointer);

    void tombstone(ReaderBucketPointer bucketPointer);

    Message getMessageAt(MessagePointer pointer);

    boolean tombstoneExists(ReaderBucketPointer bucketPointer);
}
