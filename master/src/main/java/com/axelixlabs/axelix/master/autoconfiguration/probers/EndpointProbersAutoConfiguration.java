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
package com.axelixlabs.axelix.master.autoconfiguration.probers;

import java.net.http.HttpClient;
import java.util.Arrays;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.Resource;

import com.axelixlabs.axelix.common.auth.core.SecurityContextExecutor;
import com.axelixlabs.axelix.common.domain.ActuatorEndpoints;
import com.axelixlabs.axelix.master.contract.details.InstanceDetails;
import com.axelixlabs.axelix.master.service.serde.DetailsJacksonMessageDeserializationStrategy;
import com.axelixlabs.axelix.master.service.serde.GcLogFileMessageDeserializationStrategy;
import com.axelixlabs.axelix.master.service.serde.HeapDumpMessageDeserializationStrategy;
import com.axelixlabs.axelix.master.service.state.InstanceRegistry;
import com.axelixlabs.axelix.master.service.transport.CachingEndpointProber;
import com.axelixlabs.axelix.master.service.transport.DefaultEndpointProber;
import com.axelixlabs.axelix.master.service.transport.DiscardingAbstractEndpointProber;
import com.axelixlabs.axelix.master.service.transport.EndpointProber;
import com.axelixlabs.axelix.master.service.transport.ProxyingEndpointProber;

/**
 * Configuration that creates necessary {@link EndpointProber} instances to
 * access the API on the managed service side.
 *
 * @author Mikhail Polivakha
 * @author Sergey Cherkasov
 * @author Nikita Kirillov
 * @author Vyacheslav Yanin
 */
@AutoConfiguration
public class EndpointProbersAutoConfiguration {

    private final InstanceRegistry instanceRegistry;
    private final SecurityContextExecutor securityContextExecutor;
    private final HttpClient httpClient;

    public EndpointProbersAutoConfiguration(
            InstanceRegistry instanceRegistry, SecurityContextExecutor securityContextExecutor, HttpClient httpClient) {
        this.instanceRegistry = instanceRegistry;
        this.securityContextExecutor = securityContextExecutor;
        this.httpClient = httpClient;
    }

    // Loggers
    @Bean
    public DiscardingAbstractEndpointProber setOneLoggerEndpointProber() {
        return new DiscardingAbstractEndpointProber(
                instanceRegistry, ActuatorEndpoints.SET_ONE_LOGGER, securityContextExecutor, httpClient);
    }

    @Bean
    public DiscardingAbstractEndpointProber resetLoggerLevelForLoggerEndpointProber() {
        return new DiscardingAbstractEndpointProber(
                instanceRegistry, ActuatorEndpoints.RESET_FOR_LOGGER, securityContextExecutor, httpClient);
    }

    @Bean
    public DiscardingAbstractEndpointProber setLevelForLoggerGroupEndpointProber() {
        return new DiscardingAbstractEndpointProber(
                instanceRegistry, ActuatorEndpoints.SET_FOR_LOGGER_GROUP, securityContextExecutor, httpClient);
    }

    @Bean
    public ProxyingEndpointProber getAllLoggersEndpointProber() {
        return new ProxyingEndpointProber(
                instanceRegistry, ActuatorEndpoints.GET_ALL_LOGGERS, securityContextExecutor, httpClient);
    }

    @Bean
    public ProxyingEndpointProber getOneLoggerEndpointProber() {
        return new ProxyingEndpointProber(
                instanceRegistry, ActuatorEndpoints.GET_ONE_LOGGER, securityContextExecutor, httpClient);
    }

    @Bean
    public ProxyingEndpointProber getLoggerGroupEndpointProber() {
        return new ProxyingEndpointProber(
                instanceRegistry, ActuatorEndpoints.GET_LOGGER_GROUP, securityContextExecutor, httpClient);
    }

    // Caches
    @Bean
    public DiscardingAbstractEndpointProber clearAllCachesEndpointProber() {
        return new DiscardingAbstractEndpointProber(
                instanceRegistry, ActuatorEndpoints.CLEAR_ALL_CACHES, securityContextExecutor, httpClient);
    }

    @Bean
    public DiscardingAbstractEndpointProber clearSingleCacheEndpointProber() {
        return new DiscardingAbstractEndpointProber(
                instanceRegistry, ActuatorEndpoints.CLEAR_SINGLE_CACHE, securityContextExecutor, httpClient);
    }

    @Bean
    public DiscardingAbstractEndpointProber enableCacheEndpointProver() {
        return new DiscardingAbstractEndpointProber(
                instanceRegistry, ActuatorEndpoints.ENABLE_CACHE, securityContextExecutor, httpClient);
    }

    @Bean
    public DiscardingAbstractEndpointProber disableCacheEndpointProver() {
        return new DiscardingAbstractEndpointProber(
                instanceRegistry, ActuatorEndpoints.DISABLE_CACHE, securityContextExecutor, httpClient);
    }

    @Bean
    public ProxyingEndpointProber getSingleCacheEndpointProver() {
        return new ProxyingEndpointProber(
                instanceRegistry, ActuatorEndpoints.GET_SINGLE_CACHE, securityContextExecutor, httpClient);
    }

    @Bean
    public ProxyingEndpointProber getAllCachesEndpointProver() {
        return new ProxyingEndpointProber(
                instanceRegistry, ActuatorEndpoints.GET_ALL_CACHES, securityContextExecutor, httpClient);
    }

    // Details
    @Bean
    public DefaultEndpointProber<InstanceDetails> getDetailsEndpointProber(
            DetailsJacksonMessageDeserializationStrategy deserializationStrategy) {
        return new DefaultEndpointProber<>(
                instanceRegistry,
                deserializationStrategy,
                securityContextExecutor,
                ActuatorEndpoints.GET_DETAILS,
                httpClient);
    }

    // Beans
    @Bean
    public EndpointProber<byte[]> getBeansEndpointProber() {
        return new CachingEndpointProber<>(
                new ProxyingEndpointProber(
                        instanceRegistry, ActuatorEndpoints.GET_BEANS, securityContextExecutor, httpClient),
                bytes -> Arrays.copyOf(bytes, bytes.length));
    }

    // ThreadDump
    @Bean
    public ProxyingEndpointProber getThreadDumpEndpointProber() {
        return new ProxyingEndpointProber(
                instanceRegistry, ActuatorEndpoints.GET_THREAD_DUMP, securityContextExecutor, httpClient);
    }

    @Bean
    public DiscardingAbstractEndpointProber enableThreadDumpEndpointProber() {
        return new DiscardingAbstractEndpointProber(
                instanceRegistry,
                ActuatorEndpoints.THREAD_DUMP_ENABLE_CONTENTION_MONITORING,
                securityContextExecutor,
                httpClient);
    }

    @Bean
    public DiscardingAbstractEndpointProber disableThreadDumpEndpointProber() {
        return new DiscardingAbstractEndpointProber(
                instanceRegistry,
                ActuatorEndpoints.THREAD_DUMP_DISABLE_CONTENTION_MONITORING,
                securityContextExecutor,
                httpClient);
    }

    // Metrics
    @Bean
    public ProxyingEndpointProber getMetricGroupsEndpointProver() {
        return new ProxyingEndpointProber(
                instanceRegistry, ActuatorEndpoints.GET_METRIC_GROUPS, securityContextExecutor, httpClient);
    }

    @Bean
    public ProxyingEndpointProber getSingleMetricEndpointProver() {
        return new ProxyingEndpointProber(
                instanceRegistry, ActuatorEndpoints.GET_SINGLE_METRIC, securityContextExecutor, httpClient);
    }

    // Environment Property
    @Bean
    public EndpointProber<byte[]> getAllEnvironmentEndpointProver() {
        return new ProxyingEndpointProber(
                instanceRegistry, ActuatorEndpoints.GET_ALL_ENV_PROPERTIES, securityContextExecutor, httpClient);
    }

    // HeapDump
    @Bean
    public DefaultEndpointProber<Resource> getHeapDumpEndpointProver(
            HeapDumpMessageDeserializationStrategy deserializationStrategy) {
        return new DefaultEndpointProber<>(
                instanceRegistry,
                deserializationStrategy,
                securityContextExecutor,
                ActuatorEndpoints.GET_HEAP_DUMP,
                httpClient);
    }

    // Garbage Collector Log
    @Bean
    public DefaultEndpointProber<Resource> getGcLogFileEndpointProber(
            GcLogFileMessageDeserializationStrategy deserializationStrategy) {
        return new DefaultEndpointProber<>(
                instanceRegistry,
                deserializationStrategy,
                securityContextExecutor,
                ActuatorEndpoints.GET_GC_LOG_FILE,
                httpClient);
    }

    @Bean
    public ProxyingEndpointProber getGcLogStatusEndpointProber() {
        return new ProxyingEndpointProber(
                instanceRegistry, ActuatorEndpoints.GET_STATUS_GC_LOGGING, securityContextExecutor, httpClient);
    }

    @Bean
    public DiscardingAbstractEndpointProber gcTriggerEndpointProber() {
        return new DiscardingAbstractEndpointProber(
                instanceRegistry, ActuatorEndpoints.GC_TRIGGER, securityContextExecutor, httpClient);
    }

    @Bean
    public DiscardingAbstractEndpointProber enableGcLoggingEndpointProber() {
        return new DiscardingAbstractEndpointProber(
                instanceRegistry, ActuatorEndpoints.ENABLE_GC_LOGGING, securityContextExecutor, httpClient);
    }

    @Bean
    public DiscardingAbstractEndpointProber disableGcLoggingEndpointProber() {
        return new DiscardingAbstractEndpointProber(
                instanceRegistry, ActuatorEndpoints.DISABLE_GC_LOGGING, securityContextExecutor, httpClient);
    }

    // Scheduled tasks
    @Bean
    public ProxyingEndpointProber getScheduledTasksEndpointProber() {
        return new ProxyingEndpointProber(
                instanceRegistry, ActuatorEndpoints.GET_SCHEDULED_TASKS, securityContextExecutor, httpClient);
    }

    @Bean
    public DiscardingAbstractEndpointProber executeScheduledTasksEndpointProber() {
        return new DiscardingAbstractEndpointProber(
                instanceRegistry, ActuatorEndpoints.EXECUTE_SCHEDULED_TASK, securityContextExecutor, httpClient);
    }

    @Bean
    public DiscardingAbstractEndpointProber enableScheduledTasksEndpointProber() {
        return new DiscardingAbstractEndpointProber(
                instanceRegistry, ActuatorEndpoints.ENABLE_SCHEDULED_TASK, securityContextExecutor, httpClient);
    }

    @Bean
    public DiscardingAbstractEndpointProber disableScheduledTasksEndpointProber() {
        return new DiscardingAbstractEndpointProber(
                instanceRegistry, ActuatorEndpoints.DISABLE_SCHEDULED_TASK, securityContextExecutor, httpClient);
    }

    @Bean
    public DiscardingAbstractEndpointProber modifyCronExpressionScheduledTasksEndpointProber() {
        return new DiscardingAbstractEndpointProber(
                instanceRegistry,
                ActuatorEndpoints.MODIFY_CRON_EXPRESSION_SCHEDULED_TASK,
                securityContextExecutor,
                httpClient);
    }

    @Bean
    public DiscardingAbstractEndpointProber modifyIntervalScheduledTasksEndpointProber() {
        return new DiscardingAbstractEndpointProber(
                instanceRegistry,
                ActuatorEndpoints.MODIFY_INTERVAL_SCHEDULED_TASK,
                securityContextExecutor,
                httpClient);
    }

    // Conditions
    @Bean
    public EndpointProber<byte[]> getConditionsProber() {
        return new CachingEndpointProber<>(
                new ProxyingEndpointProber(
                        instanceRegistry, ActuatorEndpoints.GET_CONDITIONS, securityContextExecutor, httpClient),
                bytes -> Arrays.copyOf(bytes, bytes.length));
    }

    // Configuration Properties
    @Bean
    public EndpointProber<byte[]> getConfigPropsProber() {
        return new ProxyingEndpointProber(
                instanceRegistry, ActuatorEndpoints.GET_CONFIG_PROPS, securityContextExecutor, httpClient);
    }

    // Dependencies (SBOM)
    @Bean
    public EndpointProber<byte[]> getDependenciesSbomProber() {
        return new CachingEndpointProber<>(new ProxyingEndpointProber(
                instanceRegistry, ActuatorEndpoints.GET_DEPENDENCIES_SBOM, securityContextExecutor, httpClient));
    }

    // Feign Client
    @Bean
    public ProxyingEndpointProber getFeignClientEndpointProber() {
        return new ProxyingEndpointProber(
                instanceRegistry, ActuatorEndpoints.GET_FEIGN_CLIENT, securityContextExecutor, httpClient);
    }
}
