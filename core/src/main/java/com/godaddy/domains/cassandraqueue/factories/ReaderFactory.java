package com.godaddy.domains.cassandraqueue.factories;

import com.goddady.cassandra.queue.api.client.QueueName;
import com.godaddy.domains.cassandraqueue.workers.Reader;

public interface ReaderFactory {
    Reader forQueue(QueueName queueName);
}
