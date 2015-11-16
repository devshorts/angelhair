package com.godaddy.domains.cassandraqueue.unittests.time;

import com.godaddy.domains.cassandraqueue.model.Clock;
import org.joda.time.Duration;
import org.joda.time.Instant;


public class TestClock implements Clock {

    public static TestClock create() {
        return new TestClock();
    }

    private Instant time = Instant.now();

    public void tick() {
        tickSeconds(1L);
    }

    public void tickSeconds(Long seconds){
        time = time.plus(Duration.standardSeconds(seconds));
    }

    @Override
    public Instant now() {
        return time;
    }
}
