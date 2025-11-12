package com.nucleonforge.axile.common.api.metrics;

import java.util.List;

import com.nucleonforge.axile.common.domain.spring.actuator.ActuatorEndpoints;

/**
 * The response to the {@link ActuatorEndpoints#METRICS} request.
 *
 * @author Mikhail Polivakha
 */
public record MetricsList(List<String> names) {}
