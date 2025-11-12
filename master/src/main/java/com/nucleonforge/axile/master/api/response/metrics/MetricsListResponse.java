package com.nucleonforge.axile.master.api.response.metrics;

import java.util.List;

/**
 * Response to the {@link MetricsApi#MAIN}.
 *
 * @param names list of metric names.
 * @author Mikhail Polivakha
 */
public record MetricsListResponse(List<String> names) {}
