package com.godaddy.domains.cassandraqueue.dataAccess;

import com.datastax.driver.core.Session;
import com.godaddy.domains.cassandraqueue.dataAccess.interfaces.QueueRepository;
import com.godaddy.domains.cassandraqueue.model.QueueName;
import com.google.inject.Inject;
import org.apache.commons.lang.NotImplementedException;

import java.util.List;

public class QueueRepositoryImpl extends RepositoryBase implements QueueRepository {

    @Inject
    public QueueRepositoryImpl(final Session session) {
        this.session = session;
    }

    private final Session session;

    @Override
    public void createQueue(final QueueName queueName) {
        throw new NotImplementedException();
    }

    @Override
    public boolean queueExists(final QueueName queueName) {
        throw new NotImplementedException();
    }

    @Override
    public List<QueueName> getQueues() {
        throw new NotImplementedException();

    }
}
