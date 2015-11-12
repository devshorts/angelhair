package com.godaddy.domains.cassandraqueue.dataAccess.interfaces;

import com.godaddy.domains.cassandraqueue.model.MonotonicIndex;

public interface MonotonicRepository {
    MonotonicIndex nextMonotonic();

    MonotonicIndex getCurrent();
}
