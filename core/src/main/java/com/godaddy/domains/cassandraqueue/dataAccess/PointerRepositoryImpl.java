package com.godaddy.domains.cassandraqueue.dataAccess;

import com.datastax.driver.core.Session;
import com.godaddy.domains.cassandraqueue.dataAccess.interfaces.PointerRepository;
import com.godaddy.domains.cassandraqueue.model.BucketPointer;
import com.godaddy.domains.cassandraqueue.model.InvisibilityMessagePointer;
import com.godaddy.domains.cassandraqueue.model.QueueName;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

public class PointerRepositoryImpl implements PointerRepository {
    @Inject
    public PointerRepositoryImpl(Session session, @Assisted QueueName queueName) {
    }

    @Override public void moveMessagePointerTo(final BucketPointer ptr) {

    }

    @Override public void moveInvisiblityPointerTo(final InvisibilityMessagePointer ptr) {

    }
}
