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
