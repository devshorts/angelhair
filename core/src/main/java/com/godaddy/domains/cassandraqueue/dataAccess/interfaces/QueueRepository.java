package com.godaddy.domains.cassandraqueue.dataAccess.interfaces;

import com.godaddy.domains.cassandraqueue.model.QueueName;

import java.util.List;

public interface QueueRepository {
    void createQueue(QueueName queueName);

    boolean queueExists(QueueName queueName);

    List<QueueName> getQueues();
}