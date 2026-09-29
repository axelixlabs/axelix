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
package com.axelixlabs.axelix.sbs.spring.core.master.insights;

import java.util.List;

import com.axelixlabs.axelix.sbs.spring.core.contract.metadata.JpaEntities;

/**
 * {@link JpaEntitiesProfileProvider} used when no JPA persistence provider is available in the instance. It
 * reports an empty registry, i.e. no JPA entities were found in the application.
 *
 * @author Mikhail Polivakha
 */
public class NoOpJpaEntitiesProfileProvider implements JpaEntitiesProfileProvider {

    @Override
    public JpaEntities getEntities() {
        return new JpaEntities().entities(List.of());
    }
}
