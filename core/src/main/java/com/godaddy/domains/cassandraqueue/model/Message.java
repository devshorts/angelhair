package com.godaddy.domains.cassandraqueue.model;

import com.datastax.driver.core.Row;
import com.godaddy.domains.cassandraqueue.dataAccess.Tables;
import com.goddady.cassandra.queue.api.client.QueueName;
import lombok.Builder;
import lombok.Data;
import org.joda.time.DateTime;

@Data
@Builder
public class Message {
    private final MonotonicIndex index;

    private final String blob;

    private DateTime nextVisiblityAt;

    private DateTime createdDate;

    private boolean isAcked;

    private int version = 0;

    private int deliveryCount = 0;

    public boolean isVisible() {
        return nextVisiblityAt == null || nextVisiblityAt.isBeforeNow();
    }

    public boolean isNotAcked() {
        return !isAcked;
    }

    public boolean isNotVisible() {
        return !isVisible();
    }

    public PopReceipt getPopReceipt() {
        return PopReceipt.from(this);
    }

    public Message createNewWithIndex(MonotonicIndex index) {
        return Message.builder()
                      .blob(blob)
                      .index(index)
                      .version(0)
                      .isAcked(false)
                      .nextVisiblityAt(nextVisiblityAt)
                      .deliveryCount(deliveryCount)
                      .createdDate(createdDate)
                      .build();
    }


    public Message withNewVersion(int version) {
        return Message.builder()
                      .blob(blob)
                      .index(index)
                      .version(version)
                      .isAcked(isAcked)
                      .nextVisiblityAt(nextVisiblityAt)
                      .deliveryCount(deliveryCount)
                      .createdDate(createdDate)
                      .build();
    }

    public static Message fromRow(final Row row) {

        return Message.builder()
                      .blob(row.getString(Tables.Message.MESSAGE))
                      .index(MonotonicIndex.valueOf(row.getLong(Tables.Message.MONOTON)))
                      .isAcked(row.getBool(Tables.Message.ACKED))
                      .version(row.getInt(Tables.Message.VERSION))
                      .deliveryCount(row.getInt(Tables.Message.DELIVERY_COUNT))
                      .nextVisiblityAt(new DateTime(row.getDate(Tables.Message.NEXT_VISIBLE_ON)))
                      .createdDate(new DateTime(row.getDate(Tables.Message.CREATED_DATE)))
                      .build();
    }
}
