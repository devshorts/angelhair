package com.godaddy.domains.cassandraqueue.unittests;

import com.godaddy.domains.cassandraqueue.dataAccess.exceptions.ExistingMonotonFoundException;
import com.godaddy.domains.cassandraqueue.factories.DataContext;
import com.godaddy.domains.cassandraqueue.factories.DataContextFactory;
import com.godaddy.domains.cassandraqueue.factories.ReaderFactory;
import com.godaddy.domains.cassandraqueue.model.Message;
import com.godaddy.domains.cassandraqueue.model.MonotonicIndex;
import com.godaddy.domains.cassandraqueue.model.PopReceipt;
import com.godaddy.domains.cassandraqueue.model.QueueName;
import com.godaddy.domains.cassandraqueue.workers.Reader;
import com.google.inject.Injector;
import org.joda.time.Duration;
import org.junit.Test;

import java.util.Optional;

import static junit.framework.TestCase.assertTrue;
import static org.assertj.core.api.Assertions.assertThat;

public class ReaderTester extends TestBase {

    @Test
    public void delivery_count_increases_after_message_expires_and_is_redelivered() throws ExistingMonotonFoundException, InterruptedException {
        final Injector defaultInjector = getDefaultInjector();

        final ReaderFactory readerFactory = defaultInjector.getInstance(ReaderFactory.class);
        final QueueName queueName = QueueName.valueOf("test_ack_next_message");

        setupQueue(queueName);

        final Reader reader = readerFactory.forQueue(queueName);

        final DataContextFactory factory = defaultInjector.getInstance(DataContextFactory.class);
        final DataContext context = factory.forQueue(queueName);

        final MonotonicIndex monoton = getTestMonoton();

        context.getMessageRepository().putMessage(
                Message.builder()
                       .blob("hi")
                       .index(monoton)
                       .build(), Duration.standardSeconds(0));

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
        final Injector defaultInjector = getDefaultInjector();

        final ReaderFactory readerFactory = defaultInjector.getInstance(ReaderFactory.class);
        final QueueName queueName = QueueName.valueOf("test_ack_next_message");

        setupQueue(queueName);

        final Reader reader = readerFactory.forQueue(queueName);

        final DataContextFactory factory = defaultInjector.getInstance(DataContextFactory.class);
        final DataContext context = factory.forQueue(queueName);

        final MonotonicIndex monoton = getTestMonoton();

        context.getMessageRepository().putMessage(
                Message.builder()
                       .blob("hi")
                       .index(monoton)
                       .build(), Duration.standardSeconds(0));

        Optional<Message> message = reader.nextMessage(Duration.standardSeconds(100L));

        assertTrue(message.get().getBlob().equals("hi"));

        boolean acked = reader.ackMessage(PopReceipt.from(message.get()));

        assertTrue(acked);
    }
}
