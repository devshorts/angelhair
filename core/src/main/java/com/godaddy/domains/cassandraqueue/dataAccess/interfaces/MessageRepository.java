package com.godaddy.domains.cassandraqueue.dataAccess.interfaces;

import com.godaddy.domains.cassandraqueue.model.ReaderBucketPointer;
import com.godaddy.domains.cassandraqueue.model.Message;
import com.godaddy.domains.cassandraqueue.model.MessagePointer;
import com.godaddy.domains.cassandraqueue.model.RepairBucketPointer;
import org.joda.time.DateTime;
import org.joda.time.Duration;

import java.util.List;
import java.util.Optional;

public interface MessageRepository {
    void putMessage(Message message, Duration initialInvisibility);

    boolean markMessageInvisible(Message message, Duration duration);

    boolean ackMessage(Message message);

    List<Message> getMessages(ReaderBucketPointer bucketPointer);

    void tombstone(ReaderBucketPointer bucketPointer);

    Message getMessageAt(MessagePointer pointer);

    Optional<DateTime> tombstoneExists(RepairBucketPointer bucketPointer);
}
