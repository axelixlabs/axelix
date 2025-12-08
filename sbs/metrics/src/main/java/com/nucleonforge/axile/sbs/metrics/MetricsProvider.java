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
package com.nucleonforge.axile.sbs.metrics;

/**
 * Base contract for classes that provide metric data.
 *
 * <p>Implementations of this abstract class should define how to collect and return
 * a snapshot of system, application, or infrastructure metrics using a {@link Metrics} object.
 *
 * @since 23.06.2025
 * @author Mikhail Polivakha
 */
public abstract class MetricsProvider {

    /**
     * Collects and returns a snapshot of metrics.
     *
     * @return a {@link Metrics} instance containing the collected metrics
     */
    public abstract Metrics scratch();
}
