/*
 * Copyright 2025-present, Nucleon Forge Software.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
