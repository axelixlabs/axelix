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
package com.nucleonforge.axile.sbs.spring.env;

import java.util.Collection;
import java.util.function.Supplier;

/**
 * Interface capable to "normalize" the property name. The normalization is the process that
 * converts a property from its specific form like {@code FOO_BAR} or {@code foo.bar[1]} to some
 * canonical view.
 * <p>
 * This is somewhat similar to relaxed binding in Spring Boot, however for various reasons we cannot
 * directly use the API of relaxed binding.
 *
 * @apiNote <a href="https://github.com/spring-projects/spring-boot/wiki/relaxed-binding-2.0">Relaxed Binding doc</a>
 * @author Mikhail Polivakha
 */
public interface PropertyNameNormalizer {

    /**
     * @param propertyName inbound property name, to be normalized
     * @return normalized property name
     */
    String normalize(String propertyName);

    /**
     * @param propertyNames the collection that holds the names of the properties that needs to be normalized.
     * @param collectionFactory the {@link Supplier} that will produce a collection into which the properties will be placed.
     * @return newly created collection that will hold the normalized names of the properties.
     * @param <C> the exact collection type.
     */
    <C extends Collection<String>> C normalizeAll(C propertyNames, Supplier<C> collectionFactory);
}
