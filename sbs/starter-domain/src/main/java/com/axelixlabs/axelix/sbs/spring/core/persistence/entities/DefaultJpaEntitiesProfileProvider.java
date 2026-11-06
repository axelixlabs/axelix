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
package com.axelixlabs.axelix.sbs.spring.core.persistence.entities;

import java.util.function.Supplier;

import org.jspecify.annotations.Nullable;

import com.axelixlabs.axelix.common.contract.metadata.JpaEntities;
import com.axelixlabs.axelix.sbs.spring.core.master.insights.JpaEntitiesProfileProvider;

/**
 * Default implementation of {@link JpaEntitiesProfileProvider}.
 *
 * @author Mikhail Polivakha
 * @author Nikita Kirillov
 */
public class DefaultJpaEntitiesProfileProvider implements JpaEntitiesProfileProvider {

    private final Supplier<JpaEntities> entitiesSupplier;

    private volatile @Nullable JpaEntities cached;

    public DefaultJpaEntitiesProfileProvider(Supplier<JpaEntities> entitiesSupplier) {
        this.entitiesSupplier = entitiesSupplier;
    }

    @Override
    public JpaEntities getEntities() {
        JpaEntities local = cached;
        if (local == null) {
            synchronized (this) {
                local = cached;
                if (local == null) {
                    local = entitiesSupplier.get();
                    cached = local;
                }
            }
        }
        return local;
    }
}
