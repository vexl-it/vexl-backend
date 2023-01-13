package com.cleevio.vexl.module.offer.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import org.springframework.lang.Nullable;

import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * For backward compatibility, we do not change the format date. Anyway, for privacy reasons, we are deleting the time.
 */
public class DateTimeSerializer extends StdSerializer<LocalDate> {

    private final static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZZZ");

    public DateTimeSerializer() {
        super(LocalDate.class);
    }

    @Override
    public void serialize(@Nullable LocalDate value, JsonGenerator jgen, SerializerProvider provider) throws IOException {
        if (value == null) {
            jgen.writeNull();
        } else {
            jgen.writeString(formatter.format(value.atStartOfDay().atZone(ZoneId.of("UTC"))));
        }
    }
}
