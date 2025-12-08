/*
 * Copyright 2025-present, Nucleon Forge Software.
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
package com.nucleonforge.axile.common.api.metrics;

import java.util.List;
import java.util.Map;

import org.jspecify.annotations.Nullable;

/**
 * The metric profile as returned by the Actuator API.
 * <p>
 * Note that we intentionally do not include any micrometer's Statistic values here.
 * The MAX, TOTAL etc. might be computed for the given array of values. So we will not
 * duplicate this information in the record. Other values, as of now, do not concern us.
 *
 * @param name the name of the given metric.
 * @param description the description of the given metric.
 * @param baseUnit the base unit of the {@link #measurements measurement values}.
 * @param measurements the array of actual measurements of the given metric.
 * @param validTagCombinations the valid combinations of tags for this metric.
 *
 * @apiNote <a href="https://docs.spring.io/spring-boot/api/rest/actuator/metrics.html#metrics.retrieving-metric">Metrics Actuator API</a>
 * @author Mikhail Polivakha
 */
public record MetricProfile(
        String name,
        @Nullable String description,
        String baseUnit,
        List<Measurement> measurements,
        List<Map<String, String>> validTagCombinations) {

    /**
     * Single metric value, measured at a particular point in time.
     *
     * @param value the value of the given metric.
     */
    public record Measurement(double value) {}
}
