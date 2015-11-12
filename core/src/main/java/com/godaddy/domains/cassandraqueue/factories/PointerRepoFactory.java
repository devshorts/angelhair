package com.godaddy.domains.cassandraqueue.factories;

import com.godaddy.domains.cassandraqueue.dataAccess.interfaces.MonotonicRepository;
import com.godaddy.domains.cassandraqueue.dataAccess.interfaces.PointerRepository;
import com.godaddy.domains.cassandraqueue.model.QueueName;

public interface PointerRepoFactory {
    PointerRepository forQueue(QueueName queueName);
}

