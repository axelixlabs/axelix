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
package com.axelixlabs.axelix.common.api.registration.insights;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The specific insight feature discovered for the given service instance.
 */
public final class InsightFeature {

    private final String featureId;
    private final boolean enabled;

    /**
     * Creates a new InsightFeature.
     *
     * @param featureId the insight feature id.
     * @param enabled   the enabled state of the insight feature.
     */
    @JsonCreator
    public InsightFeature(@JsonProperty("featureId") String featureId, @JsonProperty("enabled") boolean enabled) {
        this.featureId = featureId;
        this.enabled = enabled;
    }

    public String getFeatureId() {
        return featureId;
    }

    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public String toString() {
        return "InsightFeature{" + "featureId='" + featureId + '\'' + ", enabled=" + enabled + '}';
    }
}
