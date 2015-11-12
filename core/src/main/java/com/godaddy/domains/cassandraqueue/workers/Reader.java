package com.godaddy.domains.cassandraqueue.workers;

import com.godaddy.domains.cassandraqueue.model.Message;
import com.godaddy.domains.cassandraqueue.model.MessageId;
import org.joda.time.Duration;

import java.util.Optional;

public interface Reader {
    Optional<Message> nextMessage(Duration invisiblity);

    void ackMessage(MessageId messageId);
}
