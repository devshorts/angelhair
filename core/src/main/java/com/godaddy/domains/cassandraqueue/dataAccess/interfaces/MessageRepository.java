package com.godaddy.domains.cassandraqueue.dataAccess.interfaces;

import com.godaddy.domains.cassandraqueue.model.BucketPointer;
import com.godaddy.domains.cassandraqueue.model.Message;
import com.godaddy.domains.cassandraqueue.model.MessagePointer;
import com.godaddy.domains.cassandraqueue.model.ReaderBucketPointer;
import org.joda.time.DateTime;
import org.joda.time.Duration;

import java.util.List;
import java.util.Optional;

public interface MessageRepository {
    void putMessage(Message message, Duration initialInvisibility);

    default void putMessage(Message message) {
        putMessage(message, Duration.ZERO);
    }

    boolean markMessageInvisible(Message message, Duration duration);

    boolean ackMessage(Message message);

    List<Message> getMessages(BucketPointer bucketPointer);

    void tombstone(ReaderBucketPointer bucketPointer);

    Message getMessage(MessagePointer pointer);

    Optional<DateTime> tombstoneExists(BucketPointer bucketPointer);

}
