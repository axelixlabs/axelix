package com.nucleonforge.axile.master.service.serde.caches;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.jspecify.annotations.NonNull;

import org.springframework.stereotype.Component;

import com.nucleonforge.axile.common.api.caches.SingleCache;
import com.nucleonforge.axile.master.service.serde.JacksonMessageDeserializationStrategy;

/**
 * {@link JacksonMessageDeserializationStrategy} for {@link SingleCache}.
 *
 * @author SergeyCherkasov
 */
@Component
public class SingleCacheJacksonMessageDeserializationStrategy
        extends JacksonMessageDeserializationStrategy<SingleCache> {
    public SingleCacheJacksonMessageDeserializationStrategy(ObjectMapper objectMapper) {
        super(objectMapper);
    }

    @Override
    public @NonNull Class<SingleCache> supported() {
        return SingleCache.class;
    }
}
