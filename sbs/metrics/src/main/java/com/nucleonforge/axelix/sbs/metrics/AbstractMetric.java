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
