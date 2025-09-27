package com.nucleonforge.axile.master.service.serde;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.jspecify.annotations.NonNull;

import org.springframework.stereotype.Component;

import com.nucleonforge.axile.common.api.ManagedServiceMetadata;

/**
 * {@link JacksonMessageDeserializationStrategy} for {@link ManagedServiceMetadata}.
 *
 * @since 18.09.2025
 * @author Nikita Kirillov
 */
@Component
public class MetadataJacksonMessageDeserializationStrategy
        extends JacksonMessageDeserializationStrategy<ManagedServiceMetadata> {

    public MetadataJacksonMessageDeserializationStrategy(ObjectMapper objectMapper) {
        super(objectMapper);
    }

    @Override
    public @NonNull Class<ManagedServiceMetadata> supported() {
        return ManagedServiceMetadata.class;
    }
}
