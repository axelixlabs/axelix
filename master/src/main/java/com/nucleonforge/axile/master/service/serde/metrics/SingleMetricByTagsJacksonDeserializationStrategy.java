package com.nucleonforge.axile.master.service.serde.metrics;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.jspecify.annotations.NonNull;

import org.springframework.stereotype.Component;

import com.nucleonforge.axile.common.api.metrics.MetricProfileByTags;
import com.nucleonforge.axile.common.domain.spring.actuator.ActuatorEndpoints;
import com.nucleonforge.axile.master.service.serde.JacksonMessageDeserializationStrategy;

/**
 * {@link JacksonMessageDeserializationStrategy} for the {@link ActuatorEndpoints#SINGLE_METRIC_BY_TAGS} API.
 *
 * @since 18.11.2025
 * @author Nikita Kirillov
 */
@Component
public class SingleMetricByTagsJacksonDeserializationStrategy
        extends JacksonMessageDeserializationStrategy<MetricProfileByTags> {

    protected SingleMetricByTagsJacksonDeserializationStrategy(ObjectMapper objectMapper) {
        super(objectMapper);
    }

    @Override
    public @NonNull Class<MetricProfileByTags> supported() {
        return MetricProfileByTags.class;
    }
}
