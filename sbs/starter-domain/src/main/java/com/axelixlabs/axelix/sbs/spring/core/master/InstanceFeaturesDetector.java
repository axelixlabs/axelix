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
package com.axelixlabs.axelix.sbs.spring.core.master;

import java.util.Set;

import com.axelixlabs.axelix.common.contract.metadata.InstanceFeature;

/**
 * Detects which {@link InstanceFeature features} the current instance has, so that Axelix Master can tell the UI
 * which pages make sense for it. The concrete implementation is expected to be contributed by a given starter.
 * <p>
 * The detection is performed once the application is ready, and reflects the existing infrastructure rather than its content.
 * For instance, an instance with a cache manager has the caches feature even if no cache has been created yet, since
 * caches may be created lazily, on the first access.
 *
 * @author Mikhail Polivakha
 */
public interface InstanceFeaturesDetector {

    /**
     * @return the features the current instance has.
     */
    Set<InstanceFeature> detect();
}
