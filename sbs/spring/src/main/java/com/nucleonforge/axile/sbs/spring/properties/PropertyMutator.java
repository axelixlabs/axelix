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

import org.springframework.context.ApplicationContext;

/**
 * The interface that is capable to modify the {@link Property} value.
 * <p>
 * The implementations must reload all necessary {@link ApplicationContext} components that rely on this property.
 *
 * @since 07.04.25
 * @author Mikhail Polivakha
 */
public interface PropertyMutator {

    /**
     * Mutate the property
     *
     * @param propertyName the property name to be mutated
     * @param newValue the new value of the property
     */
    void mutate(String propertyName, String newValue);
}
