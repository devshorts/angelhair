package com.godaddy.domains.cassandraqueue.factories;

import com.godaddy.domains.cassandraqueue.dataAccess.interfaces.QueueRepository;
import com.goddady.cassandra.queue.api.client.QueueName;
import com.google.inject.Inject;
import com.google.inject.Provider;

public class DataContextFactoryImpl implements DataContextFactory {
    private final MonotonicRepoFactory monotonicRepoFactory;
    private final PointerRepoFactory pointerRepoFactory;
    private final MessageRepoFactory messageRepoFactory;
    private final Provider<QueueRepository> queueRepositoryProvider;

    @Inject
    public DataContextFactoryImpl(
            MonotonicRepoFactory monotonicRepoFactory,
            PointerRepoFactory pointerRepoFactory,
            MessageRepoFactory messageRepoFactory,
            Provider<QueueRepository> queueRepositoryProvider) {
        this.monotonicRepoFactory = monotonicRepoFactory;
        this.pointerRepoFactory = pointerRepoFactory;
        this.messageRepoFactory = messageRepoFactory;
        this.queueRepositoryProvider = queueRepositoryProvider;
    }

    public DataContext forQueue(QueueName queueName) {
        return new DataContext(
                messageRepoFactory.forQueue(queueName),
                monotonicRepoFactory.forQueue(queueName),
                pointerRepoFactory.forQueue(queueName),
                queueRepositoryProvider.get());
    }
}


