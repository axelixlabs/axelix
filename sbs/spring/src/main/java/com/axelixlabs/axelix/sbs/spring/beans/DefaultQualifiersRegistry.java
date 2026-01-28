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
package com.axelixlabs.axelix.sbs.spring.beans;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.jspecify.annotations.NonNull;

/**
 * Default in-memory registry that stores the qualifiers of a given beans.
 *
 * @author Mikhail Polivakha
 */
public class DefaultQualifiersRegistry {

    public static final DefaultQualifiersRegistry INSTANCE = new DefaultQualifiersRegistry();

    private final ConcurrentMap<String, List<String>> cache;

    private DefaultQualifiersRegistry() {
        this.cache = new ConcurrentHashMap<>(32);
    }

    @NonNull
    public List<String> getQualifiers(@NonNull String beanName) {
        return Optional.ofNullable(cache.get(beanName)).orElse(new ArrayList<>());
    }

    public void registerQualifiers(@NonNull String beanName, @NonNull List<String> qualifiers) {
        cache.compute(beanName, (s, existingQualifiers) -> {
            if (existingQualifiers == null || existingQualifiers.isEmpty()) {
                existingQualifiers = new ArrayList<>();
            }
            existingQualifiers.addAll(qualifiers);
            return existingQualifiers;
        });
    }
}
