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
        return nextVisiblityAt.map(AbstractInstant::isAfterNow).orElse(true);
    }

    public boolean isNotAcked(){
        return !isAcked;
    }

    public boolean isNotVisible(){
        return !isVisible();
    }
}
