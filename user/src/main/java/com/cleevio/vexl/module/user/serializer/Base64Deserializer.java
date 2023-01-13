package com.cleevio.vexl.module.user.serializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;
import java.util.Base64;

public class Base64Deserializer extends StdDeserializer<byte[]> {

    public Base64Deserializer() {
        super(byte[].class);
    }

    @Override
    public byte[] deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        return Base64.getDecoder().decode(p.getText());
    }
}
