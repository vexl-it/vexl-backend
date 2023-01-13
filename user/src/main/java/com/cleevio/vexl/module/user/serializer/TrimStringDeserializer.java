package com.cleevio.vexl.module.user.serializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import org.springframework.lang.Nullable;

import java.io.IOException;

public class TrimStringDeserializer extends StdDeserializer<String>  {

    public TrimStringDeserializer() {
        super(String.class);
    }

    @Override
    @Nullable
    public String deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        return p.getText() == null ? null : p.getText().trim();
    }
}
