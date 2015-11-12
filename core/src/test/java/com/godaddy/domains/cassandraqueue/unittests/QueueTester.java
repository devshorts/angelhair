package com.godaddy.domains.cassandraqueue.unittests;

import com.datastax.driver.core.Session;
import com.godaddy.domains.cassandraqueue.ServiceConfiguration;
import com.godaddy.domains.cassandraqueue.factories.DataContext;
import com.godaddy.domains.cassandraqueue.factories.DataContextFactory;
import com.godaddy.domains.cassandraqueue.model.QueueName;
import com.godaddy.domains.cassandraqueue.modules.Modules;
import com.godaddy.domains.cassandraqueue.unittests.modules.InMemorySessionProvider;
import com.godaddy.domains.cassandraqueue.unittests.modules.MockEnvironmentModule;
import com.godaddy.domains.cassandraqueue.unittests.server.LiveServer;
import com.godaddy.domains.common.test.guice.ModuleUtils;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;


public class QueueTester {
    @Test
    public void test_session() throws Exception {
        Session session = CqlDb.create();

        final LiveServer liveServer = new LiveServer();

        liveServer.getOverridableModules().add(new InMemorySessionProvider(session));

        liveServer.start();

        final String entity = liveServer.getClient("/api/v1/ping/thing").request().buildGet().submit(String.class).get();

        assertThat(entity).isEqualToIgnoringCase("thing");
    }

    @Test
    public void test_injector() throws Exception {
        final Session session = CqlDb.create();

        final Injector injector = Guice.createInjector(ModuleUtils.mergeModules(Modules.modules,
                                                                                new InMemorySessionProvider(session),
                                                                                new MockEnvironmentModule<>(new ServiceConfiguration())));

        final DataContextFactory instance = injector.getInstance(DataContextFactory.class);

        final DataContext dataContext = instance.forQueue(QueueName.valueOf("test"));
    }
}

