package com.godaddy.domains.cassandraqueue.dataAccess;

import com.godaddy.domains.cassandraqueue.model.MonotonicIndex;

public final class Tombstone {
    public static final MonotonicIndex index =   MonotonicIndex.valueOf(-2);
}
