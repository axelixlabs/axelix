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

import org.jspecify.annotations.NonNull;

import com.axelixlabs.axelix.common.api.transform.units.BaseUnit;

/**
 * Transforms the value of the given metrics. On the conceptual level
 * it acts just like a {@link java.util.function.Function}. It
 * is also a strategy interface of some sort, see the {@link #supports()} method.
 *
 * @author Mikhail Polivakha
 */
public interface BaseUnitValueTransformer {

    /**
     * Actual transformation function. Transforms the given double (which
     * is reported in the base unit as reported by {@link #supports()}) into
     * another {@link TransformedMetricValue}.
     *
     * @param value value to transform
     * @return transformed value
     */
    TransformedMetricValue transform(double value);

    /**
     * @return TransformableBaseUnit for which the current {@link BaseUnitValueTransformer} is responsible.
     */
    @NonNull
    BaseUnit supports();
}
