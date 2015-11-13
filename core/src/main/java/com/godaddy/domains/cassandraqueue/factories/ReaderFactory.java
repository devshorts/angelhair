package com.godaddy.domains.cassandraqueue.factories;

import com.godaddy.domains.cassandraqueue.model.QueueName;
import com.godaddy.domains.cassandraqueue.workers.Reader;

public interface ReaderFactory {
    Reader forQueue(QueueName queueName);
}
