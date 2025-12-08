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
package com.nucleonforge.axile.sbs.spring.integrations.rdbms;

import java.sql.Connection;

/**
 * Strategy interface for creating an {@link RDBMSIntegration} instance
 * from a {@link java.sql.Connection}. Implementations of this interface
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
