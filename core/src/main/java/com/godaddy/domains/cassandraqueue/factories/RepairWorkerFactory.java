package com.godaddy.domains.cassandraqueue.factories;

import com.godaddy.domains.cassandraqueue.model.QueueDefinition;
import com.goddady.cassandra.queue.api.client.QueueName;
import com.godaddy.domains.cassandraqueue.workers.RepairWorker;

public interface RepairWorkerFactory {
    RepairWorker forQueue(QueueDefinition definition);
}
