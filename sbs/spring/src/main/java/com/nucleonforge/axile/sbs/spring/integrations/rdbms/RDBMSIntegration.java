package com.nucleonforge.axile.sbs.spring.integrations.rdbms;

import com.nucleonforge.axile.sbs.spring.integrations.AbstractIntegration;

/**
 * Represents an integration with a relational database management system (RDBMS)
 * using the JDBC protocol.
 *
 * @since 09.07.2025
 * @author Mikhail Polivakha
 */
public class RDBMSIntegration extends AbstractIntegration {

    public RDBMSIntegration(String networkAddress, RDBMSType entityType) {
        super(networkAddress, "JDBC", entityType.getDisplayName());
    }
}
