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

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * The abstract metric value that contains common implementation for {@link MetricValue}.
 *
 * @since 09.07.25
 * @author Mikhail Polivakha
 */
public class AbstractMetric<T> implements MetricValue<T> {

    private final T value;
    private final String display;
    private final boolean alarmValue;

    @Nullable
    private final String alarmDescription;

    AbstractMetric(T value, String display, @NonNull String alarmDescription) {
        this.value = value;
        this.display = display;
        this.alarmValue = true;
        this.alarmDescription = alarmDescription;
    }

    AbstractMetric(T value, String display) {
        this.value = value;
        this.display = display;
        this.alarmValue = false;
        this.alarmDescription = null;
    }

    @Override
    public T getValue() {
        return value;
    }

    @Override
    public String getDisplayableValue() {
        return display;
    }

    @Override
    public boolean valueAlarm() {
        return alarmValue;
    }

    @Override
    public @Nullable String alarmDescription() {
        return alarmDescription;
    }
}
