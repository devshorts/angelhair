package com.godaddy.domains.cassandraqueue.unittests;

import com.godaddy.domains.cassandraqueue.factories.RepairWorkerFactory;
import com.godaddy.domains.cassandraqueue.workers.RepairWorker;
import com.google.inject.Injector;
import org.junit.Test;

public class RepairTests extends TestBase {
    @Test
    public void test_repairs(){
        final Injector defaultInjector = getDefaultInjector();

        final RepairWorkerFactory instance = defaultInjector.getInstance(RepairWorkerFactory.class);


    }
}
