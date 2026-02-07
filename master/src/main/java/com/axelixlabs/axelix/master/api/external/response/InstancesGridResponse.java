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
package com.axelixlabs.axelix.master.api.external.response;

import java.util.Collection;

import org.jspecify.annotations.Nullable;

/**
 * The grid of {@link InstanceShortProfile instances}.
 *
 * @author Mikhail Polivakha
 */
public record InstancesGridResponse(Collection<InstanceShortProfile> instances) {

    /**
     * The short profile of the particular Instance, managed by this master deployment.
     *
     * @param deployedFor a String representation for how long the service has been already deployed for.
     *
     * @author Mikhail Polivakha
     */
    public record InstanceShortProfile(
            String instanceId,
            String name,
            String serviceVersion,
            String commitShaShort,
            InstanceStatus status,
            @Nullable String deployedFor,
            String javaVersion,
            String springBootVersion) {}

    /**
     * The state of the given instance.
     *
     * @author Mikhail Polivakha
     */
    public enum InstanceStatus {
        UP,
        DOWN,
        UNKNOWN,
        RELOAD
    }
}
