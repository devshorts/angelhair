package com.godaddy.domains.cassandraqueue.dataAccess;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.Statement;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.godaddy.domains.cassandraqueue.dataAccess.interfaces.MonotonicRepository;
import com.godaddy.domains.cassandraqueue.model.MonotonicIndex;
import com.godaddy.domains.cassandraqueue.model.QueueName;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

import static com.datastax.driver.core.querybuilder.QueryBuilder.eq;
import static com.datastax.driver.core.querybuilder.QueryBuilder.set;

public class MonotonicRepoImpl extends RepositoryBase implements MonotonicRepository {
    private final Session session;
    private final QueueName queueName;

    @Inject
    public MonotonicRepoImpl(Session session, @Assisted QueueName queueName) {
        this.session = session;
        this.queueName = queueName;
    }

    @Override public MonotonicIndex nextMonotonic() {
        MonotonicIndex nextMonotonic = null;

        initializeMonotonicValue();

        while(nextMonotonic == null) {
            nextMonotonic = incrementMonotonicValue();
        }

        return nextMonotonic;
    }

    @Override public MonotonicIndex getCurrent() {
        Statement statement = QueryBuilder.select()
                                          .all()
                                          .from(Tables.Monoton.TABLE_NAME)
                                          .where(eq(Tables.Monoton.QUEUENAME, queueName));

        MonotonicIndex current = getOne(session.execute(statement), MonotonicIndex::map);

        return current == null ? MonotonicIndex.valueOf(1) : current;
    }

    private void initializeMonotonicValue() {
        Statement statement = QueryBuilder.insertInto(Tables.Monoton.TABLE_NAME)
                                          .value(Tables.Monoton.QUEUENAME, queueName)
                                          .value(Tables.Monoton.VALUE, 1)
                                          .ifNotExists();

        session.execute(statement);
    }

    private MonotonicIndex incrementMonotonicValue() {
        Long current = getCurrent().get();

        Statement stat = QueryBuilder.update(Tables.Monoton.TABLE_NAME)
                                     .with(set(Tables.Monoton.VALUE, current + 1))
                                     .where(eq(Tables.Monoton.QUEUENAME, queueName))
                                     .onlyIf(eq(Tables.Monoton.VALUE, current));

        return getOne(session.execute(stat), MonotonicIndex::map);
    }
}
