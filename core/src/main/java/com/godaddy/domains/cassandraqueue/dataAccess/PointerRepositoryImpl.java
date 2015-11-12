package com.godaddy.domains.cassandraqueue.dataAccess;

import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.Statement;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.godaddy.domains.cassandraqueue.dataAccess.interfaces.PointerRepository;
import com.godaddy.domains.cassandraqueue.model.BucketPointer;
import com.godaddy.domains.cassandraqueue.model.InvisibilityMessagePointer;
import com.godaddy.domains.cassandraqueue.model.MonotonicIndex;
import com.godaddy.domains.cassandraqueue.model.Pointer;
import com.godaddy.domains.cassandraqueue.model.PointerType;
import com.godaddy.domains.cassandraqueue.model.QueueName;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import org.apache.commons.lang.NotImplementedException;

import java.util.function.Function;

import static com.datastax.driver.core.querybuilder.QueryBuilder.eq;

public class PointerRepositoryImpl extends RepositoryBase implements PointerRepository {
    private final Session session;
    private final QueueName queueName;

    @Inject
    public PointerRepositoryImpl(Session session, @Assisted QueueName queueName) {
        this.session = session;
        this.queueName = queueName;
    }

    @Override public BucketPointer advanceMessageBucketPointer(final BucketPointer ptr) {
        throw new NotImplementedException();
    }

    @Override public void moveInvisiblityPointerTo(final MonotonicIndex destination) {

    }

    @Override public InvisibilityMessagePointer getCurrentInvisPointer() {
        return getPointer(PointerType.INVISIBILITY_POINTER, InvisibilityMessagePointer::map);
    }

    @Override public void moveMessagePointerTo(final BucketPointer ptr) {
        movePointer(PointerType.BUCKET_POINTER, ptr);
    }

    @Override public void moveInvisiblityPointerTo(InvisibilityMessagePointer ptr) {
        movePointer(PointerType.INVISIBILITY_POINTER, ptr);
    }

    @Override public BucketPointer getReaderCurrentBucket() {
        return getPointer(PointerType.BUCKET_POINTER, BucketPointer::map);
    }

    private void movePointer(PointerType pointerType, Pointer pointer) {
        Statement statement = QueryBuilder.insertInto(Tables.Pointer.TABLE_NAME)
                                          .value(Tables.Pointer.QUEUENAME, queueName.get())
                                          .value(Tables.Pointer.POINTER_TYPE, pointerType.toString())
                                          .value(Tables.Pointer.VALUE, pointer.get());

        session.execute(statement);
    }

    private <T extends Pointer> T getPointer(PointerType pointerType, Function<Row, T> mapper) {
        Statement query = QueryBuilder.select()
                                      .all()
                                      .from(Tables.Pointer.TABLE_NAME)
                                      .where(eq(Tables.Pointer.QUEUENAME, queueName.get()))
                                      .and(eq(Tables.Pointer.POINTER_TYPE, pointerType.toString()));

        return getOne(session.execute(query), mapper);
    }
}
