/*
 * Copyright 2025-present the original author or authors.
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
package com.nucleonforge.axile.master.service.discovery;

import java.util.Set;

import org.jspecify.annotations.NonNull;

import com.nucleonforge.axile.master.model.instance.Instance;

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
