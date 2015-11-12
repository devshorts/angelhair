package com.godaddy.domains.cassandraqueue.modules;

import com.godaddy.domains.cassandraqueue.dataAccess.MessageRepositoryImpl;
import com.godaddy.domains.cassandraqueue.dataAccess.MonotonicRepoImpl;
import com.godaddy.domains.cassandraqueue.dataAccess.PointerRepositoryImpl;
import com.godaddy.domains.cassandraqueue.dataAccess.QueueRepositoryImpl;
import com.godaddy.domains.cassandraqueue.dataAccess.interfaces.MessageRepository;
import com.godaddy.domains.cassandraqueue.dataAccess.interfaces.MonotonicRepository;
import com.godaddy.domains.cassandraqueue.dataAccess.interfaces.PointerRepository;
import com.godaddy.domains.cassandraqueue.dataAccess.interfaces.QueueRepository;
import com.godaddy.domains.cassandraqueue.factories.DataContextFactory;
import com.godaddy.domains.cassandraqueue.factories.DataContextFactoryImpl;
import com.godaddy.domains.cassandraqueue.factories.MessageRepoFactory;
import com.godaddy.domains.cassandraqueue.factories.MonotonicRepoFactory;
import com.godaddy.domains.cassandraqueue.factories.PointerRepoFactory;
import com.google.inject.AbstractModule;
import com.google.inject.assistedinject.FactoryModuleBuilder;

public class DataAccessModule extends AbstractModule {

    @Override protected void configure() {
        install(new FactoryModuleBuilder()
                        .implement(MessageRepository.class, MessageRepositoryImpl.class)
                        .build(MessageRepoFactory.class));

        install(new FactoryModuleBuilder()
                        .implement(PointerRepository.class, PointerRepositoryImpl.class)
                        .build(PointerRepoFactory.class));

        install(new FactoryModuleBuilder()
                        .implement(MonotonicRepository.class, MonotonicRepoImpl.class)
                        .build(MonotonicRepoFactory.class));

        bind(QueueRepository.class).to(QueueRepositoryImpl.class);
        bind(DataContextFactory.class).to(DataContextFactoryImpl.class);
    }
}
