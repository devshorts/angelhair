package com.godaddy.domains.cassandraqueue.factories;

import com.godaddy.domains.cassandraqueue.dataAccess.interfaces.MonotonicRepository;
import com.godaddy.domains.cassandraqueue.model.QueueName;

public interface MonotonicRepoFactory{
    MonotonicRepository forQueue(QueueName queueName);
}
