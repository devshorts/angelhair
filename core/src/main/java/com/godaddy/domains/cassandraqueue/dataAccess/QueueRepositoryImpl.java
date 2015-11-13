package com.godaddy.domains.cassandraqueue.dataAccess;

import com.datastax.driver.core.Session;
import com.datastax.driver.core.Statement;
import com.datastax.driver.core.querybuilder.Insert;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.querybuilder.Select;
import com.godaddy.domains.cassandraqueue.dataAccess.interfaces.QueueRepository;
import com.godaddy.domains.cassandraqueue.model.PointerType;
import com.goddady.cassandra.queue.api.client.QueueName;
import com.google.inject.Inject;

import java.util.List;

import static com.datastax.driver.core.querybuilder.QueryBuilder.*;
import static java.util.stream.Collectors.toList;

public class QueueRepositoryImpl extends RepositoryBase implements QueueRepository {

    @Inject
    public QueueRepositoryImpl(final Session session) {
        this.session = session;
    }

    private final Session session;

    @Override
    public void createQueue(final QueueName queueName) {
        initializeMonotonicValue(queueName);

        initializePointers(queueName);

        insertQueueRecord(queueName);
    }

    private void insertQueueRecord(final QueueName queueName) {
        final Insert insertQueue = QueryBuilder.insertInto(Tables.Queue.TABLE_NAME)
                                               .ifNotExists()
                                               .value(Tables.Queue.QUEUENAME, queueName.get());

        session.execute(insertQueue);
    }

    private void initializePointers(final QueueName queueName) {

        initializePointer(queueName, PointerType.BUCKET_POINTER, 0L);
        initializePointer(queueName, PointerType.INVISIBILITY_POINTER, -1L);
        initializePointer(queueName, PointerType.REPAIR_BUCKET, 0L);
    }

    private void initializeMonotonicValue(final QueueName queueName) {
        Statement statement = QueryBuilder.insertInto(Tables.Monoton.TABLE_NAME)
                                          .value(Tables.Monoton.QUEUENAME, queueName.get())
                                          .value(Tables.Monoton.VALUE, 0L)
                                          .ifNotExists();

        session.execute(statement);
    }

    private void initializePointer(final QueueName queueName, final PointerType pointerType, final Long value) {
        final Statement insert = QueryBuilder.insertInto(Tables.Pointer.TABLE_NAME)
                                             .ifNotExists()
                                             .value(Tables.Pointer.VALUE, value)
                                             .value(Tables.Pointer.QUEUENAME, queueName.get())
                                             .value(Tables.Pointer.POINTER_TYPE, pointerType.toString());

        session.execute(insert);
    }

    @Override
    public boolean queueExists(final QueueName queueName) {
        final Select.Where queryOne =
                QueryBuilder.select().all().from(Tables.Queue.TABLE_NAME).where(eq(Tables.Queue.QUEUENAME, queueName.get()));

        return getOne(session.execute(queryOne), row -> true) != null;
    }

    @Override
    public List<QueueName> getQueues() {
        final Select query = QueryBuilder.select().all().from(Tables.Queue.TABLE_NAME);

        return session.execute(query)
                      .all()
                      .stream()
                      .map(row -> QueueName.valueOf(row.getString(Tables.Queue.QUEUENAME))).collect(toList());

    }
}
