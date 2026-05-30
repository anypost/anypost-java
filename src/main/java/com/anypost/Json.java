package com.anypost;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.json.JsonMapper;

/**
 * The shared Jackson {@link ObjectMapper} and the type helpers built on it.
 *
 * <p>The mapper maps Java camelCase to wire snake_case, omits {@code null} fields
 * when serializing request bodies, and ignores unknown response fields for
 * forward compatibility.
 */
final class Json {

    static final ObjectMapper MAPPER = JsonMapper.builder()
            .propertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
            .serializationInclusion(JsonInclude.Include.NON_NULL)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .build();

    private Json() {}

    static JavaType typeOf(Class<?> type) {
        return MAPPER.getTypeFactory().constructType(type);
    }

    /** A {@link JavaType} for the {@code {data, has_more, next_cursor}} envelope of {@code element}. */
    static JavaType pageEnvelopeOf(Class<?> element) {
        return MAPPER.getTypeFactory().constructParametricType(PageEnvelope.class, element);
    }
}
