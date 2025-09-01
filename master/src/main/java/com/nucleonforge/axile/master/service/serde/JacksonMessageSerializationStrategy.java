package com.nucleonforge.axile.master.service.serde;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jspecify.annotations.NonNull;

import org.springframework.stereotype.Component;

/**
 * The {@link MessageSerializationStrategy} based on Jackson.
 *
 * @author Mikhail Polivakha
 */
@Component
public class JacksonMessageSerializationStrategy implements MessageSerializationStrategy {

    private final ObjectMapper objectMapper;

    public JacksonMessageSerializationStrategy(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public byte @NonNull [] serialize(@NonNull Object object) throws SerializationException {
        try {
            return objectMapper.writeValueAsBytes(object);
        } catch (JsonProcessingException e) {
            throw new SerializationException(e);
        }
    }
}
