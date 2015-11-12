package com.godaddy.domains.cassandraqueue.dataAccess;

import com.datastax.driver.core.Session;
import com.godaddy.domains.cassandraqueue.dataAccess.interfaces.PointerRepository;
import com.godaddy.domains.cassandraqueue.model.BucketPointer;
import com.godaddy.domains.cassandraqueue.model.InvisibilityMessagePointer;
import com.godaddy.domains.cassandraqueue.model.MonotonicIndex;
import com.godaddy.domains.cassandraqueue.model.QueueName;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import org.apache.commons.lang.NotImplementedException;

public class PointerRepositoryImpl implements PointerRepository {
    @Inject
    public PointerRepositoryImpl(Session session, @Assisted QueueName queueName) {
    }

    @Override public BucketPointer advanceMessageBucketPointer(final BucketPointer ptr) {
        throw new NotImplementedException();
    }

    @Override public void moveInvisiblityPointerTo(final MonotonicIndex destination) {

    }

    @Override public InvisibilityMessagePointer getCurrentInvisPointer() {
        throw new NotImplementedException();
    }
}
