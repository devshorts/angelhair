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
@XmlJavaTypeAdapter(value = MonotonicIndex.XmlAdapter.class)
@JsonSerialize(using = MonotonicIndex.JsonSerializeAdapter.class)
@JsonDeserialize(using = MonotonicIndex.JsonDeserializeAdapater.class)
public final class MonotonicIndex extends LongValue implements MessagePointer {
    protected MonotonicIndex(final Long value) {
        super(value);
    }

    public static MonotonicIndex valueOf(long value) {
        return new MonotonicIndex(value);
    }

    public BucketPointer toBucketPointer(int bucketSize){
        return BucketPointer.valueOf(get() / bucketSize);
    }

    public static MonotonicIndex map(Row row) {
        return MonotonicIndex.valueOf(row.getLong(Tables.Pointer.VALUE));
    }

    public static class XmlAdapter extends JaxbLongValueAdapter<MonotonicIndex> {

        @Nonnull @Override protected MonotonicIndex createNewInstance(final Long value) {
            return MonotonicIndex.valueOf(value);
        }
    }

    public static class JsonDeserializeAdapater extends JsonDeserializer<MonotonicIndex> {

        @Override public MonotonicIndex deserialize(final JsonParser jp, final DeserializationContext ctxt)
                throws IOException {
            return MonotonicIndex.valueOf(jp.getValueAsLong());
        }
    }

    public static class JsonSerializeAdapter extends JsonSerializer<MonotonicIndex> {
        @SuppressWarnings("ConstantConditions") @Override
        public void serialize(final MonotonicIndex value, final JsonGenerator jgen, final SerializerProvider provider)
                throws IOException {
            jgen.writeNumber(value.get());
        }
    }
}