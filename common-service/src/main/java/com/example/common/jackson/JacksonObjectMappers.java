package com.example.common.jackson;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/**
 * ObjectMapper instances that support {@code java.time} (LocalDate, LocalDateTime, etc.).
 * Use instead of {@code new ObjectMapper()}, which does not register JSR-310 by default.
 */
public final class JacksonObjectMappers {

    private static final ObjectMapper STANDARD = createStandard();

    private JacksonObjectMappers() {}

    public static ObjectMapper standard() {
        return STANDARD;
    }

    private static ObjectMapper createStandard() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.disable(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE);
        return mapper;
    }
}
