package com.nucleonforge.axile.master.service.serde;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.jspecify.annotations.NonNull;

import org.springframework.stereotype.Component;

import com.nucleonforge.axile.common.api.caches.CacheDispatcherClearResult;

/**
 * {@link JacksonMessageDeserializationStrategy} for {@link CacheDispatcherClearResult}.
 *
 * @since 02.10.2025
 * @author Nikita Kirillov
 */
@Component
public class CacheDispatcherJacksonMessageDeserializationStrategy
        extends JacksonMessageDeserializationStrategy<CacheDispatcherClearResult> {

    public CacheDispatcherJacksonMessageDeserializationStrategy(ObjectMapper objectMapper) {
        super(objectMapper);
    }

    @Override
    public @NonNull Class<CacheDispatcherClearResult> supported() {
        return CacheDispatcherClearResult.class;
    }
}
