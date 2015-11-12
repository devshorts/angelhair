package com.godaddy.domains.cassandraqueue.factories;

import com.godaddy.domains.cassandraqueue.model.QueueName;
import com.google.inject.Inject;

public class DataContextFactoryImpl implements DataContextFactory {
    private final MonotonicRepoFactory monotonicRepoFactory;
    private final PointerRepoFactory pointerRepoFactory;
    private final MessageRepoFactory messageRepoFactory;

    @Inject
    public DataContextFactoryImpl(MonotonicRepoFactory monotonicRepoFactory, PointerRepoFactory pointerRepoFactory, MessageRepoFactory messageRepoFactory) {
        this.monotonicRepoFactory = monotonicRepoFactory;
        this.pointerRepoFactory = pointerRepoFactory;
        this.messageRepoFactory = messageRepoFactory;
    }

    public DataContext forQueue(QueueName queueName) {
        return new DataContext(messageRepoFactory.forQueue(queueName), monotonicRepoFactory.forQueue(queueName), pointerRepoFactory.forQueue(queueName));
    }
}


