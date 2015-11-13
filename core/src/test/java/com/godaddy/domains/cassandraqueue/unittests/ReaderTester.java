package com.godaddy.domains.cassandraqueue.unittests;

import com.godaddy.domains.cassandraqueue.factories.DataContext;
import com.godaddy.domains.cassandraqueue.factories.DataContextFactory;
import com.godaddy.domains.cassandraqueue.factories.ReaderFactory;
import com.godaddy.domains.cassandraqueue.model.Message;
import com.godaddy.domains.cassandraqueue.model.MonotonicIndex;
import com.godaddy.domains.cassandraqueue.model.PopReceipt;
import com.goddady.cassandra.queue.api.client.QueueName;
import com.godaddy.domains.cassandraqueue.workers.BucketConfiguration;
import com.godaddy.domains.cassandraqueue.workers.Reader;
import com.google.inject.Injector;
import org.joda.time.Duration;
import org.junit.Before;
import org.junit.Test;

import java.util.Optional;

import static junit.framework.TestCase.assertTrue;
import static org.assertj.core.api.Assertions.assertThat;

public class ReaderTester extends TestBase {
    private Injector defaultInjector;

    private Reader reader;

    private QueueName queueName;

    private BucketConfiguration bucketConfiguration;

    @Before
    public void setup() {
        defaultInjector = getDefaultInjector();

        bucketConfiguration = defaultInjector.getInstance(BucketConfiguration.class);
    }

    @Test
    public void delivery_count_increases_after_message_expires_and_is_redelivered() throws Exception {
        setupReaderAndQueue(QueueName.valueOf("delivery_count_increases_after_message_expires_and_is_redelivered"));

        putMessage(0, "hi");

        Optional<Message> message = reader.nextMessage(Duration.standardSeconds(4));

        assertThat(message.isPresent()).isTrue();

        assertThat(message.get().getDeliveryCount()).isEqualTo(0);

        Thread.sleep(5000);

        message = reader.nextMessage(Duration.standardSeconds(4));

        assertThat(message.isPresent()).isTrue();

        assertThat(message.get().getDeliveryCount()).isEqualTo(1);
    }

    @Test
    public void test_ack_next_message() throws Exception {
        setupReaderAndQueue(QueueName.valueOf("test_ack_next_message"));

        putMessage(0, "hi");

        readAndAckMessage("hi", 100L);
    }

    @Test
    public void test_monoton_skipped() throws Exception {
        final QueueName test_monoton_skipped = QueueName.valueOf("test_monoton_skipped");

        setupReaderAndQueue(test_monoton_skipped);

        for(int i = 0; i < bucketConfiguration.getBucketSize() - 1; i++) {
            putMessage(0, "foo");

            readAndAckMessage("foo", 100L);
        }

        //last monoton of the bucket is grabbed and will be skipped over.
        defaultInjector.getInstance(DataContextFactory.class).forQueue(queueName).getMonotonicRepository().nextMonotonic();

        //Put message in new bucket, verify that message can be read after monoton was skipped and new bucket contains message.
        putMessage(0, "bar");

        readAndAckMessage("bar", 100L);
    }

    private void setupReaderAndQueue(QueueName queueName) {
        final ReaderFactory readerFactory = defaultInjector.getInstance(ReaderFactory.class);
        this.queueName = queueName;

        setupQueue(queueName);

        reader = readerFactory.forQueue(queueName);
    }

    private void putMessage(int seconds, String blob) throws Exception {
        final DataContextFactory factory = defaultInjector.getInstance(DataContextFactory.class);
        final DataContext context = factory.forQueue(queueName);

        final MonotonicIndex monoton = context.getMonotonicRepository().nextMonotonic();

        context.getMessageRepository().putMessage(
                Message.builder()
                       .blob(blob)
                       .index(monoton)
                       .build(), Duration.standardSeconds(seconds));
    }

    private void readAndAckMessage(String blob, Long invisDuration) {
        Optional<Message> message = reader.nextMessage(Duration.standardSeconds(invisDuration));

        assertTrue(message.get().getBlob().equals(blob));

        boolean acked = reader.ackMessage(PopReceipt.from(message.get()));

        assertTrue(acked);
    }

}
