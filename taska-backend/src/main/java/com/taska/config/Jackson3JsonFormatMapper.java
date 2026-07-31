package com.taska.config;

import org.hibernate.type.format.AbstractJsonFormatMapper;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

import java.lang.reflect.Type;

/**
 * Hibernate's built-in JSON mapper uses Jackson 2. This adapter persists the
 * application's Jackson 3 ({@code tools.jackson.*}) JSON types instead.
 */
public final class Jackson3JsonFormatMapper extends AbstractJsonFormatMapper {

    private final JsonMapper objectMapper = new JsonMapper();

    @Override
    protected <T> T fromString(CharSequence json, Type type) {
        try {
            return objectMapper.readValue(json.toString(), objectMapper.constructType(type));
        } catch (JacksonException exception) {
            throw new IllegalArgumentException("Could not deserialize JSON to " + type, exception);
        }
    }

    @Override
    protected <T> String toString(T value, Type type) {
        try {
            return objectMapper.writerFor(objectMapper.constructType(type)).writeValueAsString(value);
        } catch (JacksonException exception) {
            throw new IllegalArgumentException("Could not serialize JSON from " + type, exception);
        }
    }
}
