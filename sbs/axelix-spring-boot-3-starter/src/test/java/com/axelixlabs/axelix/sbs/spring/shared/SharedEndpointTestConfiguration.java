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
package com.axelixlabs.axelix.sbs.spring.shared;

import java.util.List;

import io.micrometer.core.instrument.MeterRegistry;

import org.springframework.boot.actuate.autoconfigure.condition.ConditionsReportEndpoint;
import org.springframework.boot.actuate.beans.BeansEndpoint;
import org.springframework.boot.actuate.metrics.MetricsEndpoint;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import com.axelixlabs.axelix.common.api.transform.BaseUnitParser;
import com.axelixlabs.axelix.common.api.transform.BytesMemoryBaseUnitValueTransformer;
import com.axelixlabs.axelix.common.api.transform.KilobytesMemoryBaseUnitValueTransformer;
import com.axelixlabs.axelix.sbs.spring.core.auth.JwtAuthTestConfiguration;
import com.axelixlabs.axelix.sbs.spring.core.beans.AxelixBeansEndpoint;
import com.axelixlabs.axelix.sbs.spring.core.beans.AxelixBeansEndpointTest;
import com.axelixlabs.axelix.sbs.spring.core.cache.AxelixCachesEndpoint;
import com.axelixlabs.axelix.sbs.spring.core.cache.AxelixCachesEndpointTest;
import com.axelixlabs.axelix.sbs.spring.core.cache.DefaultCacheOperationsDispatcher;
import com.axelixlabs.axelix.sbs.spring.core.conditions.AxelixConditionsEndpointTest;
import com.axelixlabs.axelix.sbs.spring.core.configprops.AxelixConfigurationPropertiesEndpointTest;
import com.axelixlabs.axelix.sbs.spring.core.configprops.SmartSanitizingFunction;
import com.axelixlabs.axelix.sbs.spring.core.details.AxelixDetailsEndpoint;
import com.axelixlabs.axelix.sbs.spring.core.details.DefaultServiceDetailsAssemblerTest;
import com.axelixlabs.axelix.sbs.spring.core.env.AxelixEnvironmentEndpointTest;
import com.axelixlabs.axelix.sbs.spring.core.env.EnvironmentTestConfig;
import com.axelixlabs.axelix.sbs.spring.core.env.PropertyNameNormalizer;
import com.axelixlabs.axelix.sbs.spring.core.gclog.AxelixGcEndpointTest;
import com.axelixlabs.axelix.sbs.spring.core.integrations.feign.AxelixFeignEndpointTest;
import com.axelixlabs.axelix.sbs.spring.core.loggers.AxelixLoggersEndpointTest;
import com.axelixlabs.axelix.sbs.spring.core.master.AxelixMetadataEndpoint;
import com.axelixlabs.axelix.sbs.spring.core.master.AxelixMetadataEndpointTest;
import com.axelixlabs.axelix.sbs.spring.core.master.CommitIdPluginShortBuildInfoProvider;
import com.axelixlabs.axelix.sbs.spring.core.master.DefaultServiceMetadataAssembler;
import com.axelixlabs.axelix.sbs.spring.core.metrics.AxelixMetricsEndpoint;
import com.axelixlabs.axelix.sbs.spring.core.metrics.AxelixMetricsEndpointTest;
import com.axelixlabs.axelix.sbs.spring.core.metrics.AxelixMetricsPublisher;
import com.axelixlabs.axelix.sbs.spring.core.metrics.DefaultAxelixMetricsPublisher;
import com.axelixlabs.axelix.sbs.spring.core.metrics.DefaultServiceMetricsGroupsAssembler;
import com.axelixlabs.axelix.sbs.spring.core.scheduled.AxelixScheduledTasksEndpointTest;
import com.axelixlabs.axelix.sbs.spring.core.threaddump.ThreadDumpManagementEndpointTest;
import com.axelixlabs.axelix.sbs.spring.core.transactions.TransactionMonitoringEndpointTest;

/**
 * Single aggregate configuration that contributes every Axelix actuator endpoint (and the per-endpoint test
 * collaborators) into the one shared {@code ApplicationContext} used by {@link AbstractEndpointIntegrationTest}.
 * <p>
 * The per-endpoint {@code @TestConfiguration}s remain nested inside their respective test classes (so that
 * assertions reading physical/enclosing class names keep working) and are pulled in here by reference.
 *
 * @author Sergey Cherkasov
 */
@Configuration
@Import({
    // EnvironmentTestConfig must precede JwtAuthTestConfiguration: it defines a non-conditional
    // securityContextExecutor, while JwtAuthTestConfiguration defines a @ConditionalOnMissingBean one that must
    // back off. It also supplies the configprops/env infrastructure beans shared with the configprops endpoint.
    EnvironmentTestConfig.class,

    // Auth (filter + JWT services) — shared by every endpoint test.
    JwtAuthTestConfiguration.class,

    // axelix-metrics
    AxelixMetricsEndpoint.class,
    MetricsEndpoint.class,
    DefaultServiceMetricsGroupsAssembler.class,
    BaseUnitParser.class,
    KilobytesMemoryBaseUnitValueTransformer.class,
    BytesMemoryBaseUnitValueTransformer.class,
    AxelixMetricsEndpointTest.AxelixMetricsEndpointTestConfiguration.class,

    // axelix-loggers
    AxelixLoggersEndpointTest.AxelixLoggersEndpointTestConfiguration.class,

    // axelix-caches
    AxelixCachesEndpoint.class,
    DefaultCacheOperationsDispatcher.class,
    AxelixCachesEndpointTest.CacheDispatcherEndpointTestConfiguration.class,

    // axelix-details
    AxelixDetailsEndpoint.class,
    DefaultServiceDetailsAssemblerTest.DefaultServiceDetailsAssemblerTestConfig.class,

    // axelix-metadata (GitInformationProvider + LibraryInformationProvider come from the shared
    // axelix-details config, which defines equivalent beans — avoids duplicate/ambiguous definitions)
    AxelixMetadataEndpoint.class,
    DefaultServiceMetadataAssembler.class,
    CommitIdPluginShortBuildInfoProvider.class,
    AxelixMetadataEndpointTest.CurrentConfig.class,

    // axelix-beans
    BeansEndpoint.class,
    AxelixBeansEndpoint.class,
    ConditionsReportEndpoint.class,
    AxelixBeansEndpointTest.CurrentConfiguration.class,

    // axelix-conditions
    AxelixConditionsEndpointTest.AxelixConditionsEndpointTestConfiguration.class,

    // axelix-thread-dump
    ThreadDumpManagementEndpointTest.ThreadDumpManagementEndpointTestConfiguration.class,

    // axelix-scheduled-tasks
    AxelixScheduledTasksEndpointTest.AxelixScheduledTasksEndpointTestConfiguration.class,

    // axelix-gc
    AxelixGcEndpointTest.AxelixGcEndpointTestTestConfiguration.class,

    // axelix-feign
    AxelixFeignEndpointTest.AxelixFeignEndpointTestConfiguration.class,

    // axelix-configprops
    AxelixConfigurationPropertiesEndpointTest.AxelixConfigurationPropertiesEndpointTestConfiguration.class,

    // axelix-transactions-monitoring
    TransactionMonitoringEndpointTest.TransactionMonitoringEndpointTestConfiguration.class,

    // axelix-env (EnvironmentTestConfig is imported above; it supplies the env/configprops infrastructure beans)
    AxelixEnvironmentEndpointTest.AxelixEnvironmentEndpointTestConfiguration.class
})
public class SharedEndpointTestConfiguration {

    /**
     * Single {@link AxelixMetricsPublisher} shared by the caches and transaction-monitoring endpoints (both
     * previously declared an identical, conflicting bean of this name in their own test configurations).
     */
    @Bean
    public AxelixMetricsPublisher axelixMetricsPublisher(MeterRegistry meterRegistry) {
        return new DefaultAxelixMetricsPublisher(meterRegistry);
    }

    /**
     * Single {@link SmartSanitizingFunction} shared by the env and configprops endpoints. Its sanitize list is the
     * union of the names each test sanitises; the function only sanitises names present in the set, so the extra
     * entries never affect the other endpoint's properties.
     */
    @Bean
    public SmartSanitizingFunction smartSanitizingFunction(PropertyNameNormalizer propertyNameNormalizer) {
        return new SmartSanitizingFunction(
                List.of(
                        "axelix.env.test.toBeSanitized",
                        "AXELIX_FOR_SANITIZATION",
                        "axelix.prop.test.tags.forSanitization",
                        "axelix.prop.test.tags.FOR_SANITIZATION"),
                propertyNameNormalizer);
    }
}
