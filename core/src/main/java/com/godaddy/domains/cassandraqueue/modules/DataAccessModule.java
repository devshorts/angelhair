package com.godaddy.domains.cassandraqueue.modules;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;
import com.godaddy.domains.cassandraqueue.ServiceConfiguration;
import com.godaddy.domains.cassandraqueue.dataAccess.MessageRepositoryImpl;
import com.godaddy.domains.cassandraqueue.dataAccess.MonotonicRepoImpl;
import com.godaddy.domains.cassandraqueue.dataAccess.PointerRepositoryImpl;
import com.godaddy.domains.cassandraqueue.dataAccess.interfaces.MessageRepository;
import com.godaddy.domains.cassandraqueue.dataAccess.interfaces.MonotonicRepository;
import com.godaddy.domains.cassandraqueue.dataAccess.interfaces.PointerRepository;
import com.godaddy.domains.cassandraqueue.factories.DataContextFactory;
import com.godaddy.domains.cassandraqueue.factories.DataContextFactoryImpl;
import com.godaddy.domains.cassandraqueue.factories.MessageRepoFactory;
import com.godaddy.domains.cassandraqueue.factories.MonotonicRepoFactory;
import com.godaddy.domains.cassandraqueue.factories.PointerRepoFactory;
import com.godaddy.domains.common.functional.LazyTwo;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import io.dropwizard.setup.Environment;

public class DataAccessModule extends AbstractModule {

    private LazyTwo<ServiceConfiguration, Environment, Session> lazy = new LazyTwo<>(this::createSession);

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

        bind(DataContextFactory.class).to(DataContextFactoryImpl.class);
    }

    @Provides
    public Session getSession(final ServiceConfiguration config, final Environment env) {
        return lazy.get(config, env);
    }

    private Session createSession(final ServiceConfiguration config, final Environment env) {
        Cluster cluster = config.getCassandraConf().build(env);

        final String keyspace = config.getCassandraConf().getKeyspace();

        return keyspace != null ? cluster.connect(keyspace) : cluster.connect();
    }
}
