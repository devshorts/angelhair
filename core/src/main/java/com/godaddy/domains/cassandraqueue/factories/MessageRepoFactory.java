package com.godaddy.domains.cassandraqueue.factories;

import com.godaddy.domains.cassandraqueue.dataAccess.interfaces.MessageRepository;
import com.godaddy.domains.cassandraqueue.model.QueueDefinition;
import com.goddady.cassandra.queue.api.client.QueueName;

public interface MessageRepoFactory {
    MessageRepository forQueue(QueueDefinition queueName);
}
