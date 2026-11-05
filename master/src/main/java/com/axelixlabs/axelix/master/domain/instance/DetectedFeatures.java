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
package com.axelixlabs.axelix.master.domain.instance;

import java.util.Collection;
import java.util.EnumSet;
import java.util.Set;

import com.axelixlabs.axelix.common.contract.metadata.InstanceFeature;

/**
 * The {@link InstanceFeature features} the {@link Instance} itself reported to have. Starters older than 1.3 do not
 * detect their features, which is why the {@link Instance} holds this value as nullable.
 *
 * @param features the features detected in the instance, possibly empty, although highly unlikely.
 *
 * @author Mikhail Polivakha
 */
public record DetectedFeatures(Set<InstanceFeature> features) {

    /**
     * @param features the features detected in the instance, possibly empty.
     * @return the detected features, kept in the declaration order of {@link InstanceFeature}.
     */
    public static DetectedFeatures of(Collection<InstanceFeature> features) {
        Set<InstanceFeature> ordered = EnumSet.noneOf(InstanceFeature.class);
        ordered.addAll(features);
        return new DetectedFeatures(ordered);
    }
}
