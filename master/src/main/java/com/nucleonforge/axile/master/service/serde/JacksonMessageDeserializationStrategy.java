package com.nucleonforge.axile.master.service.serde;

import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.jspecify.annotations.NonNull;

/**
 * {@link MessageDeserializationStrategy} based on Jackson.
 *
 * @author Mikhail Polivakha
 */
public abstract class JacksonMessageDeserializationStrategy<T> implements MessageDeserializationStrategy<T> {

    private final ObjectMapper objectMapper;

    protected JacksonMessageDeserializationStrategy(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public @NonNull T deserialize(byte @NonNull [] binary) throws DeserializationException {
        try {
            return objectMapper.readValue(binary, supported());
        } catch (IOException e) {
            throw new DeserializationException(e);
        }
    }

    /**
     * @return the instance of the {@link Class} that this {@link JacksonMessageDeserializationStrategy} supports.
     */
    public abstract @NonNull Class<T> supported();
}
