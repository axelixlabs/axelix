/*
 * Copyright 2025-present the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.nucleonforge.axile.sbs.metrics;

import java.util.HashMap;
import java.util.Map;

/**
 * A container for storing and managing metrics.
 *
 * <p>This class allows collecting metrics by name and storing them as {@link MetricValue}
 * instances. Provides methods to add metrics.
 *
 * @since 23.06.2025
 * @author Mikhail Polivakha
 */
public class Metrics {

    private final Map<String, MetricValue> metricsSource;

    /**
     * Constructs a new {@code Metrics} container with initial map capacity.
     *
     * @param size the initial capacity for the internal metric map
     */
    private Metrics(int size) {
        this.metricsSource = new HashMap<>(size);
    }

    /**
     * Factory method to create a new {@code Metrics} instance.
     *
     * @param size the initial capacity for the internal map
     * @return a new {@code Metrics} instance
     */
    public static Metrics newMetrics(int size) {
        return new Metrics(size);
    }

    /**
     * Adds a new integer metric with the specified name and value.
     *
     * @param metricName  the name of the metric
     * @param metricValue the integer value of the metric
     */
    public void fineIntMetric(String metricName, int metricValue) {
        metricsSource.put(metricName, IntegerValue.fine(metricValue));
    }

    /**
     * Adds a new integer metric with the specified name, value, and display text.
     *
     * @param metricName  the name of the metric
     * @param metricValue the integer value of the metric
     * @param display     the displayable value (e.g. formatted string or unit-suffixed value)
     */
    public void fineIntegerMetric(String metricName, int metricValue, String display) {
        metricsSource.put(metricName, IntegerValue.fine(metricValue, display));
    }
}
