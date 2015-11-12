package com.godaddy.domains.cassandraqueue.unittests;

import com.datastax.driver.core.Session;
<<<<<<< Updated upstream
import com.godaddy.domains.cassandraqueue.unittests.modules.InMemorySessionProvider;
import com.godaddy.domains.cassandraqueue.unittests.server.LiveServer;
=======
import com.godaddy.domains.cassandraqueue.model.Message;
import com.godaddy.domains.cassandraqueue.model.MonotonicIndex;
import org.jooq.lambda.tuple.Tuple2;
>>>>>>> Stashed changes
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

}

