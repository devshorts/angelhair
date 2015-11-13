package com.godaddy.domains.cassandraqueue;

import com.godaddy.domains.cassandraqueue.workers.RepairWorkerManager;
import com.godaddy.logging.Logger;
import com.google.inject.Inject;
import io.dropwizard.lifecycle.Managed;

import static com.godaddy.logging.LoggerFactory.getLogger;

@SuppressWarnings("unused")
public class Startup implements Managed {
    private static final Logger logger = getLogger(Startup.class);

    private RepairWorkerManager repairWorkerManager;

    @Inject
    public Startup(RepairWorkerManager repairWorkerManager) {
        this.repairWorkerManager = repairWorkerManager;
    }

    @Override public void start() throws Exception {
        logger.info("Starting manager");

        repairWorkerManager.start();

        logger.success("Started!");
    }

    @Override public void stop() throws Exception {
        repairWorkerManager.stop();
    }
}
