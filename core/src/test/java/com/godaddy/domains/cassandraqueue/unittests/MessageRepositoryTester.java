package com.godaddy.domains.cassandraqueue.unittests;

import com.godaddy.domains.cassandraqueue.dataAccess.interfaces.QueueRepository;
import com.godaddy.domains.cassandraqueue.factories.DataContext;
import com.godaddy.domains.cassandraqueue.factories.DataContextFactory;
import com.godaddy.domains.cassandraqueue.model.Message;
import com.godaddy.domains.cassandraqueue.model.MonotonicIndex;
import com.godaddy.domains.cassandraqueue.model.QueueName;
import com.google.inject.Injector;
import org.joda.time.Duration;
import org.junit.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class MessageRepositoryTester extends TestBase {
    @Test
    public void put_message_should_succeed() throws Exception {
        final Injector defaultInjector = getDefaultInjector();

        final DataContextFactory factory = defaultInjector.getInstance(DataContextFactory.class);
        final DataContext context = factory.forQueue(QueueName.valueOf("jakes"));

        context.getMessageRepository().putMessage(
                Message.builder()
                       .blob("hi")
                       .index(MonotonicIndex.valueOf(3))
                       .build(), Duration.standardSeconds(30));

        final List<Message> messages = context.getMessageRepository().getMessages(() -> 0L);

        assertThat(messages.size()).isEqualTo(1);

    }

    @Test
    public void ack_message_should_succeed() throws Exception {
        final Injector defaultInjector = getDefaultInjector();

        final DataContextFactory factory = defaultInjector.getInstance(DataContextFactory.class);
        final DataContext context = factory.forQueue(QueueName.valueOf("jakes"));

        context.getMessageRepository().putMessage(
                Message.builder()
                       .blob("hi")
                       .index(MonotonicIndex.valueOf(3))
                       .build(), Duration.standardSeconds(30));

        final List<Message> messages = context.getMessageRepository().getMessages(() -> 0L);

        assertThat(messages.size()).isEqualTo(1);

        final Message message = messages.get(0);

        assertThat(message.isAcked()).isEqualTo(false);

        final boolean ackSucceeded = context.getMessageRepository().ackMessage(message);

        assertThat(ackSucceeded).isEqualTo(true);

        final List<Message> ackedMessages = context.getMessageRepository().getMessages(() -> 0L);

        assertThat(ackedMessages.size()).isEqualTo(1);

        final Message ackedMessage = ackedMessages.get(0);

        assertThat(ackedMessage.isAcked()).isEqualTo(true);
    }

    @Test
    public void ack_message_after_version_changed_should_fail() throws Exception {
        final Injector defaultInjector = getDefaultInjector();

        final DataContextFactory factory = defaultInjector.getInstance(DataContextFactory.class);
        final DataContext context = factory.forQueue(QueueName.valueOf("jakes"));

        context.getMessageRepository().putMessage(
                Message.builder()
                       .blob("hi")
                       .index(MonotonicIndex.valueOf(3))
                       .build(), Duration.millis(30));

        final List<Message> messages = context.getMessageRepository().getMessages(() -> 0L);

        assertThat(messages.size()).isEqualTo(1);

        final Message message = messages.get(0);

        assertThat(message.isAcked()).isEqualTo(false);

        context.getMessageRepository().markMessageInvisible(message, Duration.standardDays(30));

        final boolean ackSucceeded = context.getMessageRepository().ackMessage(message);

        assertThat(ackSucceeded).isEqualTo(false);

        final List<Message> ackedMessages = context.getMessageRepository().getMessages(() -> 0L);

        assertThat(ackedMessages.size()).isEqualTo(1);

        final Message ackedMessage = ackedMessages.get(0);

        assertThat(ackedMessage.isAcked()).isEqualTo(false);
    }
}

