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
package com.axelixlabs.axelix.sbs.spring.env;

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
