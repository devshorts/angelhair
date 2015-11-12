package com.godaddy.domains.cassandraqueue.unittests;

import com.datastax.driver.core.Session;
import org.junit.Test;


public class QueueTester {
    @Test
    public void test_session() throws Exception {
        Session session = CqlDb.create();
    }
}
