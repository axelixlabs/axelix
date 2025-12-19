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
package com.nucleonforge.axile.sbs.spring.beans;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * Interface capable to "normalize" the bean name. The normalization is the process that
 * converts a property from its specific form like {@code WebMvcAutoConfiguration$EnableWebMvcConfiguration}
 * to some canonical view.
 *
 * @author Sergey Cherkasov
 */
public interface BeanNameNormalizer {

    default @Nullable String normalize(@Nullable String beanName) {
        return beanName != null ? normalizeInternal(beanName) : null;
    }

    /**
     * @param beanName inbound bean name, to be normalized
     * @return normalized bean name
     */
    @NonNull
    String normalizeInternal(@NonNull String beanName);
}
