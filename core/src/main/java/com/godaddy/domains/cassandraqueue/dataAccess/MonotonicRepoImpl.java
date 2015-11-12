package com.godaddy.domains.cassandraqueue.dataAccess;

import com.datastax.driver.core.Session;
import com.datastax.driver.core.Statement;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.godaddy.domains.cassandraqueue.dataAccess.interfaces.MonotonicRepository;
import com.godaddy.domains.cassandraqueue.model.MonotonicIndex;
import com.godaddy.domains.cassandraqueue.model.QueueName;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import org.apache.commons.lang.NotImplementedException;

import static com.datastax.driver.core.querybuilder.QueryBuilder.eq;

public class MonotonicRepoImpl extends RepositoryBase implements MonotonicRepository {
    private final Session session;
    private final QueueName queueName;

    @Inject
    public MonotonicRepoImpl(Session session, @Assisted QueueName queueName) {
        this.session = session;
        this.queueName = queueName;
    }

    @Override public MonotonicIndex nextMonotonic() {
        throw new NotImplementedException();
    }

    @Override public MonotonicIndex getCurrent() {
        Statement statement = QueryBuilder.select()
                                          .all()
                                          .from(Tables.Monoton.TABLE_NAME)
                                          .where(eq(Tables.Monoton.QUEUENAME, queueName));

        return getOne(session.execute(statement), MonotonicIndex::map);
    }
}
