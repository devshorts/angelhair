package com.godaddy.domains.cassandraqueue.model;

import lombok.Data;
import org.joda.time.DateTime;
import org.joda.time.base.AbstractInstant;

import java.util.Optional;

@Data
public class Message {
    private final MonotonicIndex index;

    private final String blob;

    private Optional<DateTime> nextVisiblityAt;

    private boolean isAcked;

    public boolean isVisible(){
        return !isAcked && nextVisiblityAt.map(AbstractInstant::isAfterNow).orElse(true);
    }
}
