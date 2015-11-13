package com.godaddy.domains.cassandraqueue.factories;

import com.godaddy.domains.cassandraqueue.dataAccess.interfaces.PointerRepository;
import com.goddady.cassandra.queue.api.client.QueueName;

public interface PointerRepoFactory {
    PointerRepository forQueue(QueueName queueName);
}

