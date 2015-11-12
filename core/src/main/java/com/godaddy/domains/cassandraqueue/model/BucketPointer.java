package com.godaddy.domains.cassandraqueue.model;

import com.datastax.driver.core.Row;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.godaddy.domains.cassandraqueue.dataAccess.Tables;
import com.godaddy.domains.common.valuetypes.LongValue;
import com.godaddy.domains.common.valuetypes.adapters.xml.JaxbLongValueAdapter;
import jdk.nashorn.internal.ir.annotations.Immutable;

import javax.annotation.Nonnull;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.io.IOException;

@Immutable
@XmlJavaTypeAdapter(value = BucketPointer.XmlAdapter.class)
@JsonSerialize(using = BucketPointer.JsonSerializeAdapter.class)
@JsonDeserialize(using = BucketPointer.JsonDeserializeAdapater.class)
public final class BucketPointer extends LongValue implements Pointer {
    protected BucketPointer(final Long value) {
        super(value);
    }

    public static BucketPointer valueOf(long value) {
        return new BucketPointer(value);
    }

    public BucketPointer next() {
        return new BucketPointer(get() + 1);
    }

    public MonotonicIndex startOf(int bucketsize) {
        return MonotonicIndex.valueOf(get() * bucketsize);
    }

    public static BucketPointer map(Row row) {
        return BucketPointer.valueOf(row.getLong(Tables.Pointer.VALUE));
    }

    public static class XmlAdapter extends JaxbLongValueAdapter<BucketPointer> {

        @Nonnull @Override protected BucketPointer createNewInstance(final Long value) {
            return BucketPointer.valueOf(value);
        }
    }

    public static class JsonDeserializeAdapater extends JsonDeserializer<BucketPointer> {

        @Override public BucketPointer deserialize(final JsonParser jp, final DeserializationContext ctxt)
                throws IOException {
            return BucketPointer.valueOf(jp.getValueAsLong());
        }
    }

    public static class JsonSerializeAdapter extends JsonSerializer<BucketPointer> {
        @SuppressWarnings("ConstantConditions") @Override
        public void serialize(final BucketPointer value, final JsonGenerator jgen, final SerializerProvider provider)
                throws IOException {
            jgen.writeNumber(value.get());
        }
    }
}