package com.godaddy.domains.cassandraqueue.model;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;
import org.jooq.lambda.tuple.Tuple;
import org.jooq.lambda.tuple.Tuple2;

import java.io.IOException;
import java.util.Base64;

@Data
@JsonSerialize(using = PopReceipt.JsonSerializeAdapter.class)
@JsonDeserialize(using = PopReceipt.JsonDeserializeAdapater.class)
public final class PopReceipt {
    private final int messageVersion;

    private final MonotonicIndex messageIndex;

    public static PopReceipt valueOf(String string) {
        final Tuple2<MonotonicIndex, Integer> objects = parsePopReceipt(string);

        return new PopReceipt(objects.v2, objects.v1);
    }

    public static PopReceipt from(Message message) {
        return new PopReceipt(message.getVersion(), message.getIndex());
    }

    @Override
    public String toString() {
        return getPopReceipt();
    }

    private String getPopReceipt() {
        final String receiptString = String.format("%s:%s", getMessageIndex(), getMessageVersion());

        return Base64.getEncoder().withoutPadding().encodeToString(receiptString.getBytes());
    }

    private static Tuple2<MonotonicIndex, Integer> parsePopReceipt(String popReceipt) {
        final byte[] rawReceipt = Base64.getDecoder().decode(popReceipt);

        final String receipt = new String(rawReceipt);

        final String[] components = receipt.split(":");

        return Tuple.tuple(MonotonicIndex.valueOf(Long.parseLong(components[0])), Integer.parseInt(components[1]));
    }


    public static class JsonDeserializeAdapater extends JsonDeserializer<PopReceipt> {

        @Override public PopReceipt deserialize(final JsonParser jp, final DeserializationContext ctxt)
                throws IOException {
            return PopReceipt.valueOf(jp.getValueAsString());
        }
    }

    public static class JsonSerializeAdapter extends JsonSerializer<PopReceipt> {
        @SuppressWarnings("ConstantConditions") @Override
        public void serialize(final PopReceipt value, final JsonGenerator jgen, final SerializerProvider provider)
                throws IOException {
            jgen.writeString(value.getPopReceipt());
        }
    }
}
