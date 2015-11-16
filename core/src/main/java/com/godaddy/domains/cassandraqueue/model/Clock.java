package com.godaddy.domains.cassandraqueue.model;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Instant;

public interface Clock {
    Instant now();
}

