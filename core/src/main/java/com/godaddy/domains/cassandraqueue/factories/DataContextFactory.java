package com.godaddy.domains.cassandraqueue.factories;

import com.goddady.cassandra.queue.api.client.QueueName;

public interface DataContextFactory {
    DataContext forQueue(QueueName queueName);
}
