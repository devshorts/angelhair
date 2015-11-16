package com.godaddy.domains.cassandraqueue.factories;

import com.godaddy.domains.cassandraqueue.dataAccess.interfaces.QueueRepository;
import com.godaddy.domains.cassandraqueue.model.QueueDefinition;
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

    public DataContext forQueue(QueueDefinition definition) {
        return new DataContext(
                messageRepoFactory.forQueue(definition),
                monotonicRepoFactory.forQueue(definition.getQueueName()),
                pointerRepoFactory.forQueue(definition.getQueueName()),
                queueRepositoryProvider.get());
    }
}


