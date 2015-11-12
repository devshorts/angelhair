package com.godaddy.domains.cassandraqueue.model;

import lombok.Builder;
import lombok.Data;
import org.joda.time.DateTime;
import org.joda.time.base.AbstractInstant;

import java.util.Optional;

@Data
@Builder
public class Message {
    private final MonotonicIndex index;

    private final String blob;

    private Optional<DateTime> nextVisiblityAt;

    private boolean isAcked;

    private int version = 1;

    public boolean isVisible() {
        return nextVisiblityAt.map(AbstractInstant::isAfterNow).orElse(true);
    }

    public boolean isNotAcked() {
        return !isAcked;
    }

    public boolean isNotVisible() {
        return !isVisible();
    }
}
