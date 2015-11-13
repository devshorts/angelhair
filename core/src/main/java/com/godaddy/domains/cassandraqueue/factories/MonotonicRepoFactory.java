package com.godaddy.domains.cassandraqueue.factories;

import com.godaddy.domains.cassandraqueue.dataAccess.interfaces.MonotonicRepository;
import com.goddady.cassandra.queue.api.client.QueueName;

public interface MonotonicRepoFactory{
    MonotonicRepository forQueue(QueueName queueName);
}
