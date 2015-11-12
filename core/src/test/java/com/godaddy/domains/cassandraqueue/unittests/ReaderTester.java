package com.godaddy.domains.cassandraqueue.unittests;

import com.godaddy.domains.cassandraqueue.dataAccess.interfaces.MessageRepository;
import com.godaddy.domains.cassandraqueue.model.Message;
import com.godaddy.domains.cassandraqueue.model.MonotonicIndex;
import com.godaddy.domains.cassandraqueue.model.PopReceipt;
import com.google.inject.Injector;
import org.joda.time.Duration;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ReaderTester extends TestBase {
    @Test
    public void put_message_should_succeed() throws Exception {
        final Injector defaultInjector = getDefaultInjector();

        final MessageRepository messageRepository = defaultInjector.getInstance(MessageRepository.class);

        messageRepository.putMessage(
                Message.builder()
                       .blob("hi")
                       .index(MonotonicIndex.valueOf(3))
                       .build(), Duration.standardSeconds(30));

    }
}
