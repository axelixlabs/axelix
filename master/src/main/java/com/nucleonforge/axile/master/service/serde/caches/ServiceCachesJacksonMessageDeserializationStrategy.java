package com.nucleonforge.axile.master.service.serde.caches;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.jspecify.annotations.NonNull;

import org.springframework.stereotype.Component;

import com.nucleonforge.axile.common.api.caches.ServiceCaches;
import com.nucleonforge.axile.master.service.serde.JacksonMessageDeserializationStrategy;

/**
 * {@link JacksonMessageDeserializationStrategy} for {@link ServiceCaches}.
 *
 * @author SergeyCherkasov
 */
@Component
public class ServiceCachesJacksonMessageDeserializationStrategy
        extends JacksonMessageDeserializationStrategy<ServiceCaches> {
    public ServiceCachesJacksonMessageDeserializationStrategy(ObjectMapper objectMapper) {
        super(objectMapper);
    }

    @Override
    public @NonNull Class<ServiceCaches> supported() {
        return ServiceCaches.class;
    }
}
