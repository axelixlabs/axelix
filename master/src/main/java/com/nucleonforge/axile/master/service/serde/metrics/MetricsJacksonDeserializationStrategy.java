package com.nucleonforge.axile.master.service.serde.metrics;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.jspecify.annotations.NonNull;

import org.springframework.stereotype.Component;

import com.nucleonforge.axile.common.api.metrics.MetricsList;
import com.nucleonforge.axile.common.domain.spring.actuator.ActuatorEndpoints;
import com.nucleonforge.axile.master.service.serde.JacksonMessageDeserializationStrategy;

/**
 * {@link JacksonMessageDeserializationStrategy} for the {@link ActuatorEndpoints#METRICS} API.
 *
 * @author Mikhail Polivakha
 */
@Component
public class MetricsJacksonDeserializationStrategy extends JacksonMessageDeserializationStrategy<MetricsList> {

    protected MetricsJacksonDeserializationStrategy(ObjectMapper objectMapper) {
        super(objectMapper);
    }

    @Override
    public @NonNull Class<MetricsList> supported() {
        return MetricsList.class;
    }
}
