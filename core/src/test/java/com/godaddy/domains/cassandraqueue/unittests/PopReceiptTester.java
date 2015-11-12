package com.godaddy.domains.cassandraqueue.unittests;

import com.godaddy.domains.cassandraqueue.model.Message;
import com.godaddy.domains.cassandraqueue.model.MonotonicIndex;
import org.jooq.lambda.tuple.Tuple2;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class PopReceiptTester {
    @Test
    public void test_roundtrip() throws Exception {
        final MonotonicIndex monotonicIndex = MonotonicIndex.valueOf(42);
        final int version = 3;

        Message m = Message.builder().index(monotonicIndex).version(version).build();

        final String popReceipt = m.getPopReceipt();
        System.out.println(popReceipt);

        final Tuple2<MonotonicIndex, Integer> components = Message.parsePopReceipt(popReceipt);

        assertThat(components.v1()).isEqualTo(monotonicIndex);
        assertThat(components.v2()).isEqualTo(version);
    }

}
