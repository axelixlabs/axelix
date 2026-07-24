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
package com.axelixlabs.axelix.sbs.spring.core.persistence.transaction;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.jspecify.annotations.Nullable;

import com.axelixlabs.axelix.sbs.spring.core.persistence.MethodClassKey;

/**
 * Holds the {@link TransactionDefinitionAttributes} (propagation, isolation, read-only) declared on each monitored
 * transactional method, keyed by its {@link MethodClassKey}.
 *
 * <p>Entries are registered once, during bean post-processing, while the transactional beans are being scanned, and
 * read back later when the aggregated transaction insights are assembled. Because bean creation may happen
 * concurrently, the backing map is a {@link ConcurrentHashMap}.
 *
 * @author Mikhail Polivakha
 */
public class TransactionAttributesRegistry {

    private final Map<MethodClassKey, TransactionDefinitionAttributes> attributesByMethod;

    public TransactionAttributesRegistry() {
        this.attributesByMethod = new ConcurrentHashMap<>();
    }

    public void register(MethodClassKey key, TransactionDefinitionAttributes attributes) {
        attributesByMethod.put(key, attributes);
    }

    public @Nullable TransactionDefinitionAttributes get(MethodClassKey key) {
        return attributesByMethod.get(key);
    }
}
