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

import java.util.Map;
import java.util.Set;

import com.axelixlabs.axelix.master.domain.ProbeState;

/**
 * The result of a single {@link InstancesDiscoverer#discover() discovery run}, by the discovery key of the instance.
 * Instances present in neither collection are no longer considered alive and are removed from the registry.
 *
 * @param fresh    instances probed successfully during this run
 * @param retained instances still visible to the discovery client but not probed successfully during this run,
 *                 since they were not due yet, claimed by another replica, or failed in a recoverable way (see
 *                 {@link com.axelixlabs.axelix.master.service.discovery.probe.verdict.ProbeVerdict#keepsRegistration});
 *                 the registry must keep whatever it already has for them
 * @author Marsel Semenov
 */
public record DiscoveryResult(
        Map<ProbeState.InstanceKey, DiscoveredInstanceProfile> fresh, Set<ProbeState.InstanceKey> retained) {

    public static final DiscoveryResult EMPTY = new DiscoveryResult(Map.of(), Set.of());

    public boolean isEmpty() {
        return fresh.isEmpty() && retained.isEmpty();
    }
}
