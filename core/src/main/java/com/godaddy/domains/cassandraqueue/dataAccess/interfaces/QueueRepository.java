package com.godaddy.domains.cassandraqueue.dataAccess.interfaces;

import com.godaddy.domains.cassandraqueue.model.QueueDefinition;
import com.goddady.cassandra.queue.api.client.QueueName;

import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.toList;

public interface QueueRepository {
    void createQueue(QueueDefinition definition);

    boolean queueExists(QueueName queueName);

    Optional<QueueDefinition> getQueue(QueueName queueName);

    List<QueueDefinition> getQueues();

    default List<QueueName> getQueueNames(){
        return getQueues().stream().map(QueueDefinition::getQueueName).collect(toList());
    }
}