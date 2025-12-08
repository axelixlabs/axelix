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
package com.nucleonforge.axile.sbs.metrics;

/**
 * An implementation of {@link MetricValue} that holds an integer metric.
 *
 * <p>This class stores a numeric value and display representation.
 * If no display string is provided, the numeric value is used as the displayable representation.
 *
 * @since 23.06.2025
 * @author Mikhail Polivakha
 */
public class IntegerValue extends AbstractMetric<Integer> {

    IntegerValue(Integer value, String display, String alarmDescription) {
        super(value, display, alarmDescription);
    }

    IntegerValue(Integer value, String display) {
        super(value, display);
    }

    public static IntegerValue alarming(int value, String alarmDescription, String display) {
        return new IntegerValue(value, display, alarmDescription);
    }

    public static IntegerValue alarming(int value, String alarmDescription) {
        return new IntegerValue(value, String.valueOf(value), alarmDescription);
    }

    public static IntegerValue fine(int value) {
        return new IntegerValue(value, String.valueOf(value));
    }

    public static IntegerValue fine(int value, String description) {
        return new IntegerValue(value, description);
    }
}
