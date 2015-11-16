package com.godaddy.domains.cassandraqueue.model;

import org.joda.time.Duration;
import org.joda.time.Instant;

public final class JodaClock implements Clock {

    @Override
    public Instant now() {
        return Instant.now();
    }

    @Override
    public void sleepFor(final Duration duration) throws InterruptedException {
        Thread.sleep(duration.getStandardSeconds() * 1000L);
    }
}
