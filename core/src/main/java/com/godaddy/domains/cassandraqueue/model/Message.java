package com.godaddy.domains.cassandraqueue.model;

import com.datastax.driver.core.Row;
import com.godaddy.domains.cassandraqueue.dataAccess.Tables;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;
import org.joda.time.DateTime;

import javax.validation.constraints.NotNull;

@Data
@Builder
public class Message {
    private final MonotonicIndex index;

    private final String blob;

    private DateTime nextVisiblityAt;

    private boolean isAcked;

    private int version = 0;

    public boolean isVisible() {
        return nextVisiblityAt == null || nextVisiblityAt.isBeforeNow();
    }

    public boolean isNotAcked() {
        return !isAcked;
    }

    public boolean isNotVisible() {
        return !isVisible();
    }

    public Message withNewId(MonotonicIndex index) {
        return Message.builder()
                      .blob(blob)
                      .index(index)
                      .version(0)
                      .isAcked(false)
                      .build();
    }

    public static Message fromRow(final Row row) {

        return Message.builder()
                      .blob(row.getString(Tables.Message.MESSAGE))
                      .index(MonotonicIndex.valueOf(row.getLong(Tables.Message.MONOTON)))
                      .isAcked(row.getBool(Tables.Message.ACKED))
                      .version(row.getInt(Tables.Message.VERSION))
                      .nextVisiblityAt(new DateTime(row.getDate(Tables.Message.NEXT_VISIBLE_ON)))
                      .build();
    }
}
