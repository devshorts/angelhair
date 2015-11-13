package com.godaddy.domains.cassandraqueue.unittests;

import com.datastax.driver.core.Session;
import com.godaddy.domains.cassandraqueue.modules.SessionProviderModule;
import com.godaddy.domains.cassandraqueue.unittests.server.LiveServer;
import com.godaddy.domains.common.test.guice.OverridableModule;
import com.godaddy.logging.Logger;
import com.goddady.cassandra.queue.api.client.CassandraQueueApi;
import com.goddady.cassandra.queue.api.client.MessageResponse;
import com.google.inject.Module;
import com.squareup.okhttp.ResponseBody;
import lombok.Cleanup;
import org.junit.Test;
import retrofit.Response;

import static com.godaddy.logging.LoggerFactory.getLogger;
import static org.assertj.core.api.Assertions.assertThat;

public class ApiTester extends TestBase {
    class SessionModule extends OverridableModule {

        @Override
        public Class<? extends Module> getOverridesModule() {
            return SessionProviderModule.class;
        }

        @Override
        protected void configure() {
            bind(Session.class).toInstance(session);
        }
    }

    private static final Logger logger = getLogger(ApiTester.class);


    @Test
    public void test_all_the_time() throws Exception {
        @Cleanup("stop") LiveServer server = new LiveServer();
        server.getOverridableModules().add(new SessionModule());
        server.start();


        final CassandraQueueApi client = CassandraQueueApi.createClient(server.getBaseUri().toString());

        client.createQueue("test").execute();

        client.addMessage("test", "hi").execute();

        final Response<MessageResponse> message = client.getMessage("test").execute();

        final MessageResponse body = message.body();

        assertThat(body).isNotNull();

        final String popReceipt = body.getPopReceipt();

        assertThat(popReceipt).isNotNull();

        final Response<ResponseBody> ackResponse = client.ackMessage("test", popReceipt).execute();
        assertThat(ackResponse.isSuccess()).isTrue();

    }
}
