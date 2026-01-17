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
