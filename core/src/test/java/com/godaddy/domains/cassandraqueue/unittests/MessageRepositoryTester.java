package com.godaddy.domains.cassandraqueue.unittests;

import com.godaddy.domains.cassandraqueue.factories.DataContext;
import com.godaddy.domains.cassandraqueue.factories.DataContextFactory;
import com.godaddy.domains.cassandraqueue.model.Message;
import com.godaddy.domains.cassandraqueue.model.MonotonicIndex;
import com.goddady.cassandra.queue.api.client.QueueName;
import com.google.inject.Injector;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.junit.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public class MessageRepositoryTester extends TestBase {
    @Test
    public void put_message_should_succeed() throws Exception {
        final Injector defaultInjector = getDefaultInjector();

        final DataContextFactory factory = defaultInjector.getInstance(DataContextFactory.class);
        final QueueName queueName = QueueName.valueOf("put_message_should_succeed");

        setupQueue(queueName);

        final DataContext context = factory.forQueue(queueName);

        final MonotonicIndex monoton = context.getMonotonicRepository().nextMonotonic();

        context.getMessageRepository().putMessage(
                Message.builder()
                       .blob("hi")
                       .index(monoton)
                       .build(), Duration.standardSeconds(30));

        final List<Message> messages = context.getMessageRepository().getMessages(() -> 0L);

        assertThat(messages.size()).isEqualTo(1);

    }

    @Test
    public void ack_message_should_succeed() throws Exception {
        final Injector defaultInjector = getDefaultInjector();

        final DataContextFactory factory = defaultInjector.getInstance(DataContextFactory.class);
        final QueueName queueName = QueueName.valueOf("ack_message_should_succeed");
        setupQueue(queueName);

        final DataContext context = factory.forQueue(queueName);

        final MonotonicIndex monoton = context.getMonotonicRepository().nextMonotonic();

        context.getMessageRepository().putMessage(
                Message.builder()
                       .blob("hi")
                       .index(monoton)
                       .build(), Duration.standardSeconds(30));


        final Message message = context.getMessageRepository().getMessage(monoton);

        assertThat(message.isAcked()).isFalse();

        final boolean ackSucceeded = context.getMessageRepository().ackMessage(message);
        assertThat(ackSucceeded).isTrue();

        final Message ackedMessage = context.getMessageRepository()
                                            .getMessage(message.getIndex());

        assertThat(ackedMessage.isAcked()).isTrue();
    }

    @Test
    public void ack_message_after_version_changed_should_fail() throws Exception {
        final Injector defaultInjector = getDefaultInjector();

        final DataContextFactory factory = defaultInjector.getInstance(DataContextFactory.class);
        final QueueName queueName = QueueName.valueOf("ack_message_after_version_changed_should_fail");

        setupQueue(queueName);

        final DataContext context = factory.forQueue(queueName);

        final MonotonicIndex monoton = context.getMonotonicRepository().nextMonotonic();

        context.getMessageRepository().putMessage(
                Message.builder()
                       .blob("hi")
                       .index(monoton)
                       .build(), Duration.millis(30));

        final Message message = context.getMessageRepository().getMessage(monoton);

        assertThat(message.isAcked()).isFalse();

        context.getMessageRepository().markMessageInvisible(message, Duration.standardDays(30), true);

        final boolean ackSucceeded = context.getMessageRepository().ackMessage(message);
        assertThat(ackSucceeded).isFalse();

        final Message ackedMessage = context.getMessageRepository()
                                            .getMessage(message.getIndex());
        assertThat(ackedMessage.isAcked()).isFalse();
    }

    @Test
    public void an_added_tombstone_should_exist() throws Exception {
        final Injector defaultInjector = getDefaultInjector();

        final DataContextFactory factory = defaultInjector.getInstance(DataContextFactory.class);
        final QueueName queueName = QueueName.valueOf("an_added_tombstone_should_exist");

        setupQueue(queueName);

        final DataContext context = factory.forQueue(queueName);

        final MonotonicIndex monoton = context.getMonotonicRepository().nextMonotonic();

        context.getMessageRepository().tombstone(monoton.toBucketPointer(1));

        final Optional<DateTime> tombstoneExists = context.getMessageRepository().tombstoneExists(monoton.toBucketPointer(1));

        assertThat(tombstoneExists.isPresent()).isTrue();
    }
}

