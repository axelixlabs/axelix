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
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.axelixlabs.axelix.common.api.integrations.rdbms.RDBMSIntegration;
import com.axelixlabs.axelix.common.api.integrations.rdbms.RDBMSType;
import com.axelixlabs.axelix.sbs.spring.core.integration.IntegrationComponentDiscoverer;
import com.axelixlabs.axelix.sbs.spring.core.utils.StringUtils;

/**
 * {@link IntegrationComponentDiscoverer} that is capable to discover {@link RDBMSIntegration} instances.
 *
 * @since 08.07.25
 * @author Mikhail Polivakha
 */
public class RDBMSIntegrationComponentDiscoverer implements IntegrationComponentDiscoverer<RDBMSIntegration> {

    private final Set<DataSource> dataSources;
    private final Map<RDBMSType, DatabaseIntegrationInstantiationStrategy> instantiationStrategies;

    private static final Log LOG = LogFactory.getLog(RDBMSIntegrationComponentDiscoverer.class);

    public RDBMSIntegrationComponentDiscoverer(
            Set<DataSource> dataSources, List<DatabaseIntegrationInstantiationStrategy> instantiationStrategies) {
        this.dataSources = dataSources;
        this.instantiationStrategies = instantiationStrategies.stream()
                .collect(Collectors.toMap(
                        DatabaseIntegrationInstantiationStrategy::supportedType,
                        it -> it,
                        (a, b) -> a,
                        () -> new EnumMap<>(RDBMSType.class)));
    }

    @Override
    public Set<RDBMSIntegration> discoverIntegrations() {

        Set<RDBMSIntegration> databaseIntegrations = new HashSet<>();

        for (DataSource dataSource : dataSources) {
            inspectDataSource(dataSource, databaseIntegrations);
        }

        return databaseIntegrations;
    }

    private void inspectDataSource(DataSource dataSource, Set<RDBMSIntegration> databaseIntegrations) {
        try (Connection connection = dataSource.getConnection()) {

            DatabaseMetaData metaData = connection.getMetaData();

            String driverName = metaData.getDriverName();

            for (RDBMSType value : RDBMSType.values()) {
                if (value.getAliases().stream().anyMatch(alias -> StringUtils.containsIgnoreCase(driverName, alias))) {
                    var instantiationStrategy = instantiationStrategies.get(value);
                    if (instantiationStrategy != null) {
                        databaseIntegrations.add(instantiationStrategy.instantiate(connection));
                    } else {
                        LOG.warn(
                                "Unable to find the appropriate DatabaseIntegrationInstantiationStrategy in order to equip for "
                                        + value);
                    }
                }
            }

        } catch (SQLException e) {
            LOG.warn("Unable to get the connection in order to introspect the database integration", e);
        }
    }
}
