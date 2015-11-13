package com.godaddy.domains.cassandraqueue.unittests;

import com.godaddy.domains.cassandraqueue.workers.RepairWorkerManager;
import com.google.inject.Injector;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class RepairWorkerManagerTester extends TestBase {

    @Test
    public void test_leader() throws Exception {
        final Injector defaultInjector = getDefaultInjector();

        final RepairWorkerManager repairWorkerManager1 = defaultInjector.getInstance(RepairWorkerManager.class);
        final RepairWorkerManager repairWorkerManager2 = defaultInjector.getInstance(RepairWorkerManager.class);
        final RepairWorkerManager repairWorkerManager3 = defaultInjector.getInstance(RepairWorkerManager.class);

        //assertThat(repairWorkerManager1.isLeader() || repairWorkerManager2.isLeader() || repairWorkerManager3.isLeader()).isTrue();
    }

}