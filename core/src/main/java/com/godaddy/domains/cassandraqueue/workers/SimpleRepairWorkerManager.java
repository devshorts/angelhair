package com.godaddy.domains.cassandraqueue.workers;

import com.godaddy.domains.cassandraqueue.dataAccess.interfaces.QueueRepository;
import com.godaddy.domains.cassandraqueue.factories.RepairWorkerFactory;
import com.google.inject.Inject;
import org.apache.commons.collections.CollectionUtils;

import java.util.List;

import static java.util.stream.Collectors.toList;

public class SimpleRepairWorkerManager implements RepairWorkerManager{
    private final QueueRepository queueRepo;
    private final RepairWorkerFactory repairWorkerFactory;
    private List<RepairWorker> activeRepairWorkers;

    @Inject
    public SimpleRepairWorkerManager(QueueRepository queueRepo, RepairWorkerFactory repairWorkerFactory) {
        this.queueRepo = queueRepo;
        this.repairWorkerFactory = repairWorkerFactory;
    }

    @Override public void start() {
        activeRepairWorkers = queueRepo.getQueues().stream().map(repairWorkerFactory::forQueue).collect(toList());

        activeRepairWorkers.forEach(RepairWorker::start);
    }

    @Override public void stop() {
        if(!CollectionUtils.isEmpty(activeRepairWorkers)){
            activeRepairWorkers.forEach(RepairWorker::stop);
        }
    }
}
