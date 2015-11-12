package com.godaddy.domains.cassandraqueue.model;

import lombok.Builder;
import lombok.Data;
import org.apache.commons.lang.NotImplementedException;
import org.joda.time.DateTime;
import org.joda.time.base.AbstractInstant;
import org.jooq.lambda.tuple.Tuple;
import org.jooq.lambda.tuple.Tuple2;

import java.util.Base64;
import java.util.Optional;

@Data
@Builder
public class Message {
    private final MonotonicIndex index;

    private final String blob;

    private Optional<DateTime> nextVisiblityAt;

    private boolean isAcked;

    private int version;

    public boolean isVisible(){
        return nextVisiblityAt.map(AbstractInstant::isAfterNow).orElse(true);
    }

    public boolean isNotAcked(){
        return !isAcked;
    }

    public boolean isNotVisible(){
        return !isVisible();
    }

    public String getPopReceipt() {
        final String receiptString = String.format("%s:%s", index, version);

        return Base64.getEncoder().withoutPadding().encodeToString(receiptString.getBytes());
    }

    public static Tuple2<MonotonicIndex, Integer> parsePopReceipt(String popReceipt){
        final byte[] rawReceipt = Base64.getDecoder().decode(popReceipt);

        final String receipt = new String(rawReceipt);

        final String[] components = receipt.split(":");

        return Tuple.tuple(MonotonicIndex.valueOf(Long.parseLong(components[0])), Integer.parseInt(components[1]));
    }
}
