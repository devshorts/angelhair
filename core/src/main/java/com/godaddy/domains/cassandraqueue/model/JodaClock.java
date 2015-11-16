package com.godaddy.domains.cassandraqueue.model;

import org.joda.time.Instant;

public final class JodaClock implements Clock {

    @Override
    public Instant now() {
        return Instant.now();
    }
}
