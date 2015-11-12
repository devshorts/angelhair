package com.godaddy.domains.cassandraqueue.factories;

import com.godaddy.domains.cassandraqueue.model.QueueName;

public interface DataContextFactory {
    DataContext forQueue(QueueName queueName);
}
