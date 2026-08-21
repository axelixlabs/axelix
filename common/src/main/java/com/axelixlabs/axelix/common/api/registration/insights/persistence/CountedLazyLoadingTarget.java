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
package com.axelixlabs.axelix.common.api.registration.insights.persistence;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import com.axelixlabs.axelix.common.api.LazyLoadingTarget;

/**
 * Aggregated count of lazy-loading (N+1) occasions for a particular association.
 *
 * @author Mikhail Polivakha
 */
public class CountedLazyLoadingTarget {

    private final LazyLoadingTarget target;
    private final int count;

    @JsonCreator
    public CountedLazyLoadingTarget(
            @JsonProperty("target") LazyLoadingTarget target, @JsonProperty("count") int count) {
        this.target = target;
        this.count = count;
    }

    public LazyLoadingTarget getTarget() {
        return target;
    }

    public int getCount() {
        return count;
    }
}
