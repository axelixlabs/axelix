package com.nucleonforge.axile.master.service.serde;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.jspecify.annotations.NonNull;

import org.springframework.stereotype.Component;

import com.nucleonforge.axile.common.api.ConditionsFeed;

/**
 * {@link JacksonMessageDeserializationStrategy} for {@link ConditionsFeed}.
 *
 * @since 16.10.2025
 * @author Nikita Kirillov
 */
@Component
public class ConditionsJacksonMessageDeserializationStrategy
        extends JacksonMessageDeserializationStrategy<ConditionsFeed> {

    public ConditionsJacksonMessageDeserializationStrategy(ObjectMapper objectMapper) {
        super(objectMapper);
    }

    @Override
    public @NonNull Class<ConditionsFeed> supported() {
        return ConditionsFeed.class;
    }
}
