package com.godaddy.domains.cassandraqueue.dataAccess;

import com.datastax.driver.core.Session;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.Statement;
import com.godaddy.domains.cassandraqueue.dataAccess.interfaces.PointerRepository;
import com.godaddy.domains.cassandraqueue.model.BucketPointer;
import com.godaddy.domains.cassandraqueue.model.InvisibilityMessagePointer;
import com.godaddy.domains.cassandraqueue.model.PointerType;
import com.godaddy.domains.cassandraqueue.model.QueueName;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

public class PointerRepositoryImpl implements PointerRepository {
    private final Session session;
    private final QueueName queueName;

    @Inject
    public PointerRepositoryImpl(Session session, @Assisted QueueName queueName) {
        this.session = session;
        this.queueName = queueName;
    }

    @Override public void moveMessagePointerTo(final BucketPointer ptr) {
        movePointer(PointerType.BUCKET_POINTER, ptr.get());
    }

    @Override public void moveInvisiblityPointerTo(final InvisibilityMessagePointer ptr) {
        movePointer(PointerType.INVISIBILITY_POINTER, ptr.get());
    }

    private void movePointer(PointerType pointerType, Long pointerValue) {
        Statement statement = QueryBuilder.insertInto(Tables.Pointer.TABLE_NAME)
                                          .value(Tables.Pointer.QUEUENAME, queueName.get())
                                          .value(Tables.Pointer.POINTER_TYPE, pointerType.toString())
                                          .value(Tables.Pointer.VALUE, pointerValue);

        session.execute(statement);
    }
}
