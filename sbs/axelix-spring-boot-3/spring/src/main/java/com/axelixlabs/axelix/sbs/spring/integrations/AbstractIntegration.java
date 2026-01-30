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
package com.axelixlabs.axelix.sbs.spring.integrations;

/**
 * Base implementation of the {@link Integration} interface that provides common logic
 * and holds core integration parameters such as network address, protocol, and entity type.
 *
 * <p>This class is intended to be extended by specific types of integrations,
 * which may add additional behavior or properties as needed.</p>
 *
 * @since 05.07.2025
 * @author Mikhail Polivakha
 */
public abstract non-sealed class AbstractIntegration implements Integration {

    private final String networkAddress;
    private final String protocol;
    private final String entityType;

    protected AbstractIntegration(String networkAddress, String protocol, String entityType) {
        this.networkAddress = networkAddress;
        this.protocol = protocol;
        this.entityType = entityType;
    }

    @Override
    public String entityType() {
        return entityType;
    }

    @Override
    public String protocol() {
        return protocol;
    }

    @Override
    public String networkAddress() {
        return networkAddress;
    }
}
