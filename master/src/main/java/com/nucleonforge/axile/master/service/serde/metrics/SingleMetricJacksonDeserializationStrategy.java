package com.nucleonforge.axile.master.service.serde.metrics;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.jspecify.annotations.NonNull;

import org.springframework.stereotype.Component;

import com.nucleonforge.axile.common.api.metrics.MetricProfile;
import com.nucleonforge.axile.common.domain.spring.actuator.ActuatorEndpoints;
import com.nucleonforge.axile.master.service.serde.JacksonMessageDeserializationStrategy;

/**
 * {@link JacksonMessageDeserializationStrategy} for the {@link ActuatorEndpoints#SINGLE_METRIC} API.
 *
 * @author Mikhail Polivakha
 */
@Component
public class SingleMetricJacksonDeserializationStrategy extends JacksonMessageDeserializationStrategy<MetricProfile> {

    protected SingleMetricJacksonDeserializationStrategy(ObjectMapper objectMapper) {
        super(objectMapper);
    }

    @Override
    public @NonNull Class<MetricProfile> supported() {
        return MetricProfile.class;
    }
}
