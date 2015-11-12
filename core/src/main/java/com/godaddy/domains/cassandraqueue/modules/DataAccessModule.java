package com.godaddy.domains.cassandraqueue.modules;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;
import com.godaddy.domains.cassandraqueue.ServiceConfiguration;
import com.godaddy.domains.common.functional.LazyTwo;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import io.dropwizard.setup.Environment;

public class DataAccessModule extends AbstractModule {

    private LazyTwo<ServiceConfiguration, Environment, Session> lazy = new LazyTwo<>(this::createSession);

    @Override protected void configure() {

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
