package com.godaddy.domains.cassandraqueue.factories;

import com.godaddy.domains.cassandraqueue.dataAccess.interfaces.MessageRepository;
import com.godaddy.domains.cassandraqueue.model.QueueName;

public interface MessageRepoFactory {
    MessageRepository forQueue(QueueName queueName);
}
