package com.godaddy.domains.cassandraqueue.model;

import com.datastax.driver.core.Row;

public interface Pointer<T extends Pointer> {
    Long get();
}
