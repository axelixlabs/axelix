package com.nucleonforge.axile.master.service.serde;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.jspecify.annotations.NonNull;

import org.springframework.stereotype.Component;

import com.nucleonforge.axile.common.api.registration.ServiceMetadata;

/**
 * {@link JacksonMessageDeserializationStrategy} for {@link ServiceMetadata}.
 *
 * @since 18.09.2025
 * @author Nikita Kirillov
 */
@Component
public class MetadataJacksonMessageDeserializationStrategy
        extends JacksonMessageDeserializationStrategy<ServiceMetadata> {

    public MetadataJacksonMessageDeserializationStrategy(ObjectMapper objectMapper) {
        super(objectMapper);
    }

    @Override
    public @NonNull Class<ServiceMetadata> supported() {
        return ServiceMetadata.class;
    }
}
