package com.godaddy.domains.cassandraqueue.factories;

import com.godaddy.domains.cassandraqueue.model.QueueName;
import com.godaddy.domains.cassandraqueue.workers.RepairWorker;

public interface RepairWorkerFactory {
    RepairWorker forQueue(QueueName queueName);
}
