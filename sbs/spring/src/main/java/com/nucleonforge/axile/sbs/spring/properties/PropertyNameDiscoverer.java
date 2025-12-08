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
package com.nucleonforge.axile.sbs.spring.properties;

import org.jspecify.annotations.Nullable;

import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertySource;

/**
 * Interface for discovering the actual property name as it is stored in
 * the {@link ConfigurableEnvironment}.
 *
 * @author Sergey Cherkasov
 * @author Mikhail Polivakha
 */
public interface PropertyNameDiscoverer {

    /**
     * Discovers the primary property name.
     * <p>
     * If the property name exists in multiple {@link PropertySource Property Sources}, the resulting name will be
     * from the property source with the highest priority.
     *
     * @param propertyName property name in any form (upper/lower case, dots/underscores/dashes separated etc.)
     * @return discovered {@code propertyName}, or {@code null} if no property with the given name is not found
     */
    @Nullable
    String discover(String propertyName);
}
