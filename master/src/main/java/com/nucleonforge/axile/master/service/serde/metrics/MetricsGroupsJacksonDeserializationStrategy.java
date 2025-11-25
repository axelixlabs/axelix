package com.nucleonforge.axile.master.service.serde.metrics;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.jspecify.annotations.NonNull;

import org.springframework.stereotype.Component;

import com.nucleonforge.axile.common.api.metrics.AxileMetricsGroups;
import com.nucleonforge.axile.common.domain.spring.actuator.ActuatorEndpoints;
import com.nucleonforge.axile.master.service.serde.JacksonMessageDeserializationStrategy;

/**
 * {@link JacksonMessageDeserializationStrategy} for the {@link ActuatorEndpoints#METRICS_GROUP} API.
 *
 * @author Mikhail Polivakha
 */
@Component
public class MetricsGroupsJacksonDeserializationStrategy
        extends JacksonMessageDeserializationStrategy<AxileMetricsGroups> {

    public MetricsGroupsJacksonDeserializationStrategy(ObjectMapper objectMapper) {
        super(objectMapper);
    }

    @Override
    public @NonNull Class<AxileMetricsGroups> supported() {
        return AxileMetricsGroups.class;
    }
}
