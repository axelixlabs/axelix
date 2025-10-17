package com.nucleonforge.axile.sbs.spring.integrations.rdbms;

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

import com.nucleonforge.axile.sbs.spring.integrations.IntegrationComponentDiscoverer;
import com.nucleonforge.axile.sbs.spring.utils.StringUtils;

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
