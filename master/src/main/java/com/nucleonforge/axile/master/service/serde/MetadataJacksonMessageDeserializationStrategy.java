package com.nucleonforge.axile.master.service.serde;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.jspecify.annotations.NonNull;

import org.springframework.stereotype.Component;

import com.nucleonforge.axile.common.api.AxileMetadata;

/**
 * {@link JacksonMessageDeserializationStrategy} for {@link AxileMetadata}.
 *
 * @since 18.09.2025
 * @author Nikita Kirillov
 */
@Component
public class MetadataJacksonMessageDeserializationStrategy
        extends JacksonMessageDeserializationStrategy<AxileMetadata> {

    public MetadataJacksonMessageDeserializationStrategy(ObjectMapper objectMapper) {
        super(objectMapper);
    }

    @Override
    public @NonNull Class<AxileMetadata> supported() {
        return AxileMetadata.class;
    }
}
