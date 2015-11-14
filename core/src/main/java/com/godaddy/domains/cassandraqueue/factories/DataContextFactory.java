package com.godaddy.domains.cassandraqueue.factories;

import com.godaddy.domains.cassandraqueue.model.QueueDefinition;

public interface DataContextFactory {
    DataContext forQueue(QueueDefinition definition);
}
