package com.godaddy.domains.cassandraqueue.unittests;

import com.godaddy.domains.cassandraqueue.model.Message;
import com.godaddy.domains.cassandraqueue.model.MonotonicIndex;
import com.godaddy.domains.cassandraqueue.model.PopReceipt;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class PopReceiptTester {
    @Test
    public void test_round_trip() throws Exception {
        final MonotonicIndex monotonicIndex = MonotonicIndex.valueOf(42);
        final int version = 3;

        Message m = Message.builder().index(monotonicIndex).version(version).build();

        final PopReceipt popReceipt = PopReceipt.from(m);

        System.out.println(popReceipt);

        final PopReceipt components = PopReceipt.valueOf(popReceipt.toString());

        assertThat(components.getMessageIndex()).isEqualTo(monotonicIndex);
        assertThat(components.getMessageVersion()).isEqualTo(version);
    }
}
