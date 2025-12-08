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

import org.jspecify.annotations.Nullable;

/**
 * Interface that represents the value of the exported metric.
 *
 * @since 23.06.2025
 * @author Mikhail Polivakha
 */
public interface MetricValue<T> {

    /**
     * @return the value itself, used for possible compositions and computations
     */
    T getValue();

    /**
     * @return displayable value (i.e. to be displayed on the end-user side)
     */
    String getDisplayableValue();

    /**
     * @return true if the value of the given metric is not within a rang that is considered healthy
     */
    boolean valueAlarm();

    /**
     * @return the description that explains why this metric value is not good. Might be null if {@link #valueAlarm()} is false.
     */
    @Nullable
    String alarmDescription();
}
