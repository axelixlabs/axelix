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
package com.axelixlabs.axelix.sbs.spring.core.integrations.rdbms;

import java.sql.Connection;

import com.axelixlabs.axelix.common.api.integrations.rdbms.RDBMSIntegration;
import com.axelixlabs.axelix.common.api.integrations.rdbms.RDBMSType;

/**
 * Strategy interface for creating an {@link RDBMSIntegration} instance
 * from a {@link Connection}. Implementations of this interface
 * are responsible for instantiating the integration metadata for a
 * specific RDBMS type.
 *
 * @see RDBMSIntegration
 * @see RDBMSType
 *
 * @since 07.07.2025
 * @author Mikhail Polivakha
 */
public interface DatabaseIntegrationInstantiationStrategy {

    /**
     * Instantiates an {@link RDBMSIntegration} using the provided database {@link Connection}.
     *
     * @param connection the JDBC connection to the database
     * @return a new {@link RDBMSIntegration} representing this database integration
     */
    RDBMSIntegration instantiate(Connection connection);

    /**
     * Returns the supported {@link RDBMSType} that this strategy is capable of handling.
     *
     * @return the supported RDBMS type
     */
    RDBMSType supportedType();
}
