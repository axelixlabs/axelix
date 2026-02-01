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
package com.axelixlabs.axelix.common.api.metrics;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.jspecify.annotations.Nullable;

/**
 * The metric profile as returned by the Actuator API.
 * <p>
 * Note that we intentionally do not include any micrometer's Statistic values here.
 * The MAX, TOTAL etc. might be computed for the given array of values. So we will not
 * duplicate this information in the record. Other values, as of now, do not concern us.
 *
 * @apiNote <a href="https://docs.spring.io/spring-boot/api/rest/actuator/metrics.html#metrics.retrieving-metric">Metrics Actuator API</a>
 * @author Mikhail Polivakha
 */
public final class MetricProfile {

    private final String name;

    @Nullable
    private final String description;

    private final String baseUnit;
    private final List<Measurement> measurements;
    private final List<Map<String, String>> validTagCombinations;

    /**
     * Creates a new MetricProfile.
     *
     * @param name                  the name of the given metric.
     * @param description           the description of the given metric.
     * @param baseUnit              the base unit of the {@link #measurements()} measurement values}.
     * @param measurements          the array of actual measurements of the given metric.
     * @param validTagCombinations  the valid combinations of tags for this metric.
     */
    public MetricProfile(
            String name,
            @Nullable String description,
            String baseUnit,
            List<Measurement> measurements,
            List<Map<String, String>> validTagCombinations) {
        this.name = name;
        this.description = description;
        this.baseUnit = baseUnit;
        this.measurements = measurements;
        this.validTagCombinations = validTagCombinations;
    }

    public String name() {
        return name;
    }

    @Nullable
    public String description() {
        return description;
    }

    public String baseUnit() {
        return baseUnit;
    }

    public List<Measurement> measurements() {
        return measurements;
    }

    public List<Map<String, String>> validTagCombinations() {
        return validTagCombinations;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MetricProfile that = (MetricProfile) o;
        return Objects.equals(name, that.name)
                && Objects.equals(description, that.description)
                && Objects.equals(baseUnit, that.baseUnit)
                && Objects.equals(measurements, that.measurements)
                && Objects.equals(validTagCombinations, that.validTagCombinations);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, description, baseUnit, measurements, validTagCombinations);
    }

    @Override
    public String toString() {
        return "MetricProfile{"
                + "name='"
                + name
                + '\''
                + ", description='"
                + description
                + '\''
                + ", baseUnit='"
                + baseUnit
                + '\''
                + ", measurements="
                + measurements
                + ", validTagCombinations="
                + validTagCombinations
                + '}';
    }

    /**
     * Single metric value, measured at a particular point in time.
     */
    public static final class Measurement {

        private final double value;

        /**
         * Creates a new Measurement.
         *
         * @param value the value of the given metric.
         */
        public Measurement(double value) {
            this.value = value;
        }

        public double value() {
            return value;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Measurement that = (Measurement) o;
            return Double.compare(that.value, value) == 0;
        }

        @Override
        public int hashCode() {
            return Objects.hash(value);
        }

        @Override
        public String toString() {
            return "Measurement{" + "value=" + value + '}';
        }
    }
}
