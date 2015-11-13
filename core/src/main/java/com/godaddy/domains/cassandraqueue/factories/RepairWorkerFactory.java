package com.godaddy.domains.cassandraqueue.factories;

import com.goddady.cassandra.queue.api.client.QueueName;
import com.godaddy.domains.cassandraqueue.workers.RepairWorker;

public interface RepairWorkerFactory {
    RepairWorker forQueue(QueueName queueName);
}
