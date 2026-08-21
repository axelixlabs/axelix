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
package com.axelixlabs.axelix.common.api.transform;

import java.util.Objects;

import com.axelixlabs.axelix.common.api.transform.units.BaseUnit;

/**
 * Transformed base unit.
 */
public final class TransformedMetricValue {

    private final BaseUnit baseUnit;
    private final double value;

    /**
     * Creates a new TransformedMetricValue.
     *
     * @param baseUnit the base unit of value
     * @param value the value
     */
    public TransformedMetricValue(BaseUnit baseUnit, double value) {
        this.baseUnit = baseUnit;
        this.value = value;
    }

    public BaseUnit baseUnit() {
        return baseUnit;
    }

    public double value() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        TransformedMetricValue that = (TransformedMetricValue) o;
        return Double.compare(that.value, value) == 0 && Objects.equals(baseUnit, that.baseUnit);
    }

    @Override
    public int hashCode() {
        return Objects.hash(baseUnit, value);
    }

    @Override
    public String toString() {
        return "TransformedMetricValue{" + "baseUnit=" + baseUnit + ", value=" + value + '}';
    }
}
