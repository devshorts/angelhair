package com.godaddy.domains.cassandraqueue.workers;

import com.godaddy.domains.cassandraqueue.model.Message;
import com.godaddy.domains.cassandraqueue.model.MessageId;

public interface Reader{
    Message nextMessage();

    void ackMessage(MessageId messageId);
}
