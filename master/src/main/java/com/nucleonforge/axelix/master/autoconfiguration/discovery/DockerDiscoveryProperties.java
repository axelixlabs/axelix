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
package com.nucleonforge.axelix.master.autoconfiguration.discovery;

import java.util.Set;

/**
 * Properties related to autodiscovery in Docker environments
 *
 * @see DiscoveryAutoConfiguration.DockerDiscoveryAutoConfiguration
 * @author Sergey Cherkasov
 */
@SuppressWarnings("NullAway")
public class DockerDiscoveryProperties {

    private DiscoveryFilters filters;

    /**
     * Filters to be applied during discovery of managed services.
     *
     */
    public static class DiscoveryFilters {

        /**
         * Networks to search for services, considering only those where the service is registered.
         *
         */
        private Set<String> networksName;

        public Set<String> getNetworksName() {
            return networksName;
        }

        public DiscoveryFilters setNetworksName(Set<String> networksName) {
            this.networksName = networksName;
            return this;
        }
    }

    public DiscoveryFilters getFilters() {
        return filters;
    }

    public DockerDiscoveryProperties setFilters(DiscoveryFilters filters) {
        this.filters = filters;
        return this;
    }
}
