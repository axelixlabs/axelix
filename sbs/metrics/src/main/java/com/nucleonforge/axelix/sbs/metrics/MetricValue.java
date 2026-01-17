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

import org.jspecify.annotations.Nullable;

/**
 * Interface that represents the value of the exported metric.
 *
 * @since 23.06.2025
 * @author Mikhail Polivakha
 */
public interface MetricValue<T> {

    /**
     * @return the value itself, used for possible compositions and computations
     */
    T getValue();

    /**
     * @return displayable value (i.e. to be displayed on the end-user side)
     */
    String getDisplayableValue();

    /**
     * @return true if the value of the given metric is not within a rang that is considered healthy
     */
    boolean valueAlarm();

    /**
     * @return the description that explains why this metric value is not good. Might be null if {@link #valueAlarm()} is false.
     */
    @Nullable
    String alarmDescription();
}
