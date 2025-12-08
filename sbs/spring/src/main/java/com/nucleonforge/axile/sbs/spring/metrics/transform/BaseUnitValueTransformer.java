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
package com.nucleonforge.axile.sbs.spring.metrics.transform;

import org.jspecify.annotations.NonNull;

import com.nucleonforge.axile.sbs.spring.metrics.transform.units.BaseUnit;

/**
 * Transforms the value of the given metrics. On the conceptual level
 * it acts just like a {@link java.util.function.Function}. It
 * is also a strategy interface of some sort, see the {@link #supports()} method.
 *
 * @author Mikhail Polivakha
 */
public interface BaseUnitValueTransformer {

    /**
     * Actual transformation function. Transforms the given double (which
     * is reported in the base unit as reported by {@link #supports()}) into
     * another {@link TransformedMetricValue}.
     *
     * @param value value to transform
     * @return transformed value
     */
    TransformedMetricValue transform(double value);

    /**
     * @return TransformableBaseUnit for which the current {@link BaseUnitValueTransformer} is responsible.
     */
    @NonNull
    BaseUnit supports();
}
