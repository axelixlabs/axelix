/*
 * Copyright (C) 2025-2026 Axelix Labs
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package com.nucleonforge.axelix.sbs.metrics;

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
