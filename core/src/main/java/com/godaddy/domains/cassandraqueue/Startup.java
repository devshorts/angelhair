package com.godaddy.domains.cassandraqueue;

import com.godaddy.domains.cassandraqueue.workers.RepairWorkerManager;
import com.google.inject.Inject;
import io.dropwizard.lifecycle.Managed;

public class Startup implements Managed {
    private RepairWorkerManager repairWorkerManager;

    @Inject
    public Startup(RepairWorkerManager repairWorkerManager) {
        this.repairWorkerManager = repairWorkerManager;
    }

    @Override public void start() throws Exception {
        repairWorkerManager.start();
    }

    @Override public void stop() throws Exception {
        repairWorkerManager.stop();
    }
}
