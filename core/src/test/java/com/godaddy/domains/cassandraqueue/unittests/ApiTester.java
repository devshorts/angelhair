package com.godaddy.domains.cassandraqueue.unittests;

import com.godaddy.domains.cassandraqueue.unittests.modules.InMemorySessionProvider;
import com.godaddy.domains.cassandraqueue.unittests.server.LiveServer;
import com.godaddy.logging.Logger;
import com.goddady.cassandra.queue.api.client.CassandraQueueApi;
import com.goddady.cassandra.queue.api.client.GetMessageResponse;
import com.goddady.cassandra.queue.api.client.QueueCreateOptions;
import com.goddady.cassandra.queue.api.client.QueueName;
import com.squareup.okhttp.ResponseBody;
import lombok.Cleanup;
import org.junit.Ignore;
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

        final QueueName queueName = QueueName.valueOf("test");

        client.createQueue(new QueueCreateOptions(queueName)).execute();

        client.addMessage(queueName, "hi").execute();

        final Response<GetMessageResponse> message = client.getMessage(queueName).execute();

        final GetMessageResponse body = message.body();

        assertThat(body).isNotNull();

        final String popReceipt = body.getPopReceipt();

        assertThat(popReceipt).isNotNull();

        final Response<ResponseBody> ackResponse = client.ackMessage(queueName, popReceipt).execute();

        assertThat(ackResponse.isSuccess()).isTrue();
    }

    @Test
    @Ignore
    public void demo_invis_client() throws Exception {
        @Cleanup("stop") LiveServer server = new LiveServer();
        server.getOverridableModules().add(new InMemorySessionProvider(session));
        server.start();

        final CassandraQueueApi client = CassandraQueueApi.createClient(server.getBaseUri().toString());

        final QueueName queueName = QueueName.valueOf("test");

        client.createQueue(new QueueCreateOptions(queueName)).execute();

        int count = 100;

        for (int i = 0; i < count; i++) {
            client.addMessage(queueName, Integer.valueOf(i).toString()).execute();
        }

        for (int i = 0; i < 100; i++) {
            final Response<GetMessageResponse> message = client.getMessage(queueName, 3L).execute();

            final GetMessageResponse body = message.body();

            assertThat(body).isNotNull();

            final String popReceipt = body.getPopReceipt();

            System.out.println(String.format("Message id: %s, Delivery count %s", body.getMessage(), body.getDeliveryCount()));

            if (i % 5 == 0) {
            }
            else {
                assertThat(popReceipt).isNotNull();

                final Response<ResponseBody> ackResponse = client.ackMessage(queueName, popReceipt).execute();

                assertThat(ackResponse.isSuccess()).isTrue();
            }
        }
    }

}
