package com.godaddy.domains.cassandraqueue.dataAccess;

import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.Statement;
import com.datastax.driver.core.querybuilder.Clause;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.godaddy.domains.cassandraqueue.dataAccess.interfaces.PointerRepository;
import com.godaddy.domains.cassandraqueue.model.InvisibilityMessagePointer;
import com.godaddy.domains.cassandraqueue.model.Pointer;
import com.godaddy.domains.cassandraqueue.model.PointerType;
import com.godaddy.domains.cassandraqueue.model.QueueName;
import com.godaddy.domains.cassandraqueue.model.ReaderBucketPointer;
import com.godaddy.domains.cassandraqueue.model.RepairBucketPointer;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

import java.util.function.Function;

import static com.datastax.driver.core.querybuilder.QueryBuilder.eq;
import static com.datastax.driver.core.querybuilder.QueryBuilder.gt;
import static com.datastax.driver.core.querybuilder.QueryBuilder.lt;
import static com.datastax.driver.core.querybuilder.QueryBuilder.set;

public class PointerRepositoryImpl extends RepositoryBase implements PointerRepository {
    private final Session session;
    private final QueueName queueName;

    @Inject
    public PointerRepositoryImpl(Session session, @Assisted QueueName queueName) {
        this.session = session;
        this.queueName = queueName;
    }

    @Override public ReaderBucketPointer advanceMessageBucketPointer(final ReaderBucketPointer original, final ReaderBucketPointer next) {
        return movePointer(PointerType.BUCKET_POINTER, original, next, pointerEqualsClause(original));
    }

    //if destination is less than current pointer value, move to destination.
    //if original = current pointer move to destination.
    @Override public InvisibilityMessagePointer moveInvisiblityPointerTo(
            final InvisibilityMessagePointer original, final InvisibilityMessagePointer destination) {
        return null;
    }

    @Override public RepairBucketPointer advanceRepairBucketPointer(final RepairBucketPointer original, final RepairBucketPointer next) {
        return movePointer(PointerType.REPAIR_BUCKET, original, next, pointerEqualsClause(original));
    }

    @Override public InvisibilityMessagePointer getCurrentInvisPointer() {
        return getPointer(PointerType.INVISIBILITY_POINTER, InvisibilityMessagePointer::map);
    }

    @Override public ReaderBucketPointer getReaderCurrentBucket() {
        return getPointer(PointerType.BUCKET_POINTER, ReaderBucketPointer::map);
    }

    @Override public RepairBucketPointer getRepairCurrentBucketPointer() {
        return getPointer(PointerType.REPAIR_BUCKET, RepairBucketPointer::map);
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

    private <T extends Pointer> T movePointer(PointerType pointerType, T pointer, T destination, Clause clause) {
        Statement statement = QueryBuilder.update(Tables.Pointer.TABLE_NAME)
                                          .with(set(Tables.Pointer.VALUE, pointer.get()))
                                          .where(eq(Tables.Pointer.QUEUENAME, queueName.get()))
                                          .and(eq(Tables.Pointer.POINTER_TYPE, pointerType.toString()))
                                          .onlyIf(clause);

        return session.execute(statement)
                      .wasApplied() ? destination : pointer;
    }

    private Clause pointerEqualsClause(Pointer pointer) {
        return eq(Tables.Pointer.VALUE, pointer.get());
    }
}
