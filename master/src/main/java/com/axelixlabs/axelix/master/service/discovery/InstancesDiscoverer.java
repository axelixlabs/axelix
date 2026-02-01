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
package com.axelixlabs.axelix.master.service.discovery;

import java.util.Set;

import org.jspecify.annotations.NonNull;

import com.axelixlabs.axelix.master.domain.Instance;

/**
 * The SPI interface for discovering {@link Instance instances} of running applications.
 *
 * <p>
 * There are, essentially, two ways to configure the deployment of the master and starters:
 * either master itself needs to discover instances, or the instances register themselves in
 * the master. This SPI interface exists specifically to implement the first approach.
 * <p>
 * Implementations may rely on certain environment to be present, such as K8S or consul, or
 * Netflix Eureka to for instance.
 *
 * @author Mikhail Polivakha
 */
public interface InstancesDiscoverer {

    /**
     * Perform actual discovery.
     */
    @NonNull
    Set<@NonNull Instance> discover();

    /**
     * Return the discovered {@link Set} of {@link Instance instance references}.
     * Safe variation of {@link #discover()}.
     */
    @NonNull
    default Set<@NonNull Instance> discoverSafely() {
        try {
            return discover();
        } catch (Throwable t) {
            t.printStackTrace();
            return Set.of();
        }
    }
}
