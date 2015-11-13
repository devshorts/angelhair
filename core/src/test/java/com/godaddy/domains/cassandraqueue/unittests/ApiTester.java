package com.godaddy.domains.cassandraqueue.unittests;

import com.godaddy.domains.cassandraqueue.unittests.modules.InMemorySessionProvider;
import com.godaddy.domains.cassandraqueue.unittests.server.LiveServer;
import com.godaddy.logging.Logger;
import com.goddady.cassandra.queue.api.client.CassandraQueueApi;
import com.goddady.cassandra.queue.api.client.MessageResponse;
import com.squareup.okhttp.ResponseBody;
import lombok.Cleanup;
import org.junit.Test;
import retrofit.Response;

import static com.godaddy.logging.LoggerFactory.getLogger;
import static org.assertj.core.api.Assertions.assertThat;

public class ApiTester extends TestBase {

    private static final Logger logger = getLogger(ApiTester.class);

    @Test
    public void test_client_can_create_put_and_ack() throws Exception {
        @Cleanup("stop") LiveServer server = new LiveServer();
        server.getOverridableModules().add(new InMemorySessionProvider(session));
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
