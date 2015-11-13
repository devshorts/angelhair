package com.godaddy.domains.cassandraqueue.unittests;

import com.godaddy.domains.cassandraqueue.factories.DataContext;
import com.godaddy.domains.cassandraqueue.factories.DataContextFactory;
import com.goddady.cassandra.queue.api.client.QueueName;
import com.godaddy.domains.cassandraqueue.unittests.modules.InMemorySessionProvider;
import com.godaddy.domains.cassandraqueue.unittests.server.LiveServer;
import com.google.inject.Injector;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;


public class QueueTester extends TestBase {
    @Test
    public void test_session() throws Exception {
        final LiveServer liveServer = new LiveServer();

        liveServer.getOverridableModules().add(new InMemorySessionProvider(session));

        liveServer.start();

        final String entity = liveServer.getClient("/api/v1/ping/thing").request().buildGet().submit(String.class).get();

        assertThat(entity).isEqualToIgnoringCase("thing");
    }

    @Test
    public void test_injector() throws Exception {
        final Injector injector = getDefaultInjector();

        final DataContextFactory instance = injector.getInstance(DataContextFactory.class);

        final DataContext dataContext = instance.forQueue(QueueName.valueOf("test"));

        assertThat(dataContext).isNotNull();
    }


}

