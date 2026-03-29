package com.vn.nhom2.serializer;


import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.vn.nhom2.util.StringUtil;

import java.io.IOException;
import java.util.List;

public class ImageSerializer extends JsonSerializer<String> {

    @Override
    public void serialize(String s, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        List<String> strs = List.of(s.split("\\|\\|\\|\\|"));
        jsonGenerator.writeStartArray();
        for (String str : strs) {
            if (!StringUtil.isNullOrEmpty(str)) {
                jsonGenerator.writeString(str);
            }
        }
        jsonGenerator.writeEndArray();
    }
}
