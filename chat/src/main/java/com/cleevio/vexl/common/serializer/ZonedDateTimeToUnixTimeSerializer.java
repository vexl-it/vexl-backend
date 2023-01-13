package com.cleevio.vexl.common.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;
import java.time.ZonedDateTime;

public class ZonedDateTimeToUnixTimeSerializer extends StdSerializer<ZonedDateTime> {

    public ZonedDateTimeToUnixTimeSerializer() {
        super(ZonedDateTime.class);
    }

    @Override
    public void serialize(ZonedDateTime zonedDateTime, JsonGenerator gen, SerializerProvider serializerProvider) throws IOException {
        if (zonedDateTime == null) {
            gen.writeNull();
        } else {
            gen.writeNumber(zonedDateTime.toInstant().toEpochMilli());
        }
    }
}
