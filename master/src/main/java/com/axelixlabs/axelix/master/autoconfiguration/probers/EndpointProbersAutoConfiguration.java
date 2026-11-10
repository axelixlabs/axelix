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

import tools.jackson.databind.ObjectMapper;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.Resource;

import com.axelixlabs.axelix.common.contract.configprops.ConfigurationPropertiesFeed;
import com.axelixlabs.axelix.common.contract.details.InstanceDetails;
import com.axelixlabs.axelix.common.contract.env.EnvironmentFeed;
import com.axelixlabs.axelix.common.contract.masterping.MasterPing;
import com.axelixlabs.axelix.common.contract.metadata.BasicRegistrationMetadata;
import com.axelixlabs.axelix.master.domain.ActuatorEndpoints;
import com.axelixlabs.axelix.master.service.auth.StarterAuthenticationTokenProvider;
import com.axelixlabs.axelix.master.service.serde.DefaultJacksonMessageDeserializationStrategy;
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
    private final StarterAuthenticationTokenProvider tokenProvider;
    private final HttpClient httpClient;

    public EndpointProbersAutoConfiguration(
            InstanceRegistry instanceRegistry,
            StarterAuthenticationTokenProvider tokenProvider,
            HttpClient httpClient) {
        this.instanceRegistry = instanceRegistry;
        this.tokenProvider = tokenProvider;
        this.httpClient = httpClient;
    }

    // Logs
    @Bean
    public ProxyingEndpointProber getLogEntriesTailEndpointProber() {
        return new ProxyingEndpointProber(
                instanceRegistry, ActuatorEndpoints.GET_LOG_ENTRIES_TAIL, tokenProvider, httpClient);
    }

    // Loggers
    @Bean
    public DiscardingAbstractEndpointProber setOneLoggerEndpointProber() {
        return new DiscardingAbstractEndpointProber(
                instanceRegistry, ActuatorEndpoints.SET_ONE_LOGGER, tokenProvider, httpClient);
    }

    @Bean
    public DiscardingAbstractEndpointProber resetLoggerLevelForLoggerEndpointProber() {
        return new DiscardingAbstractEndpointProber(
                instanceRegistry, ActuatorEndpoints.RESET_FOR_LOGGER, tokenProvider, httpClient);
    }

    @Bean
    public DiscardingAbstractEndpointProber setLevelForLoggerGroupEndpointProber() {
        return new DiscardingAbstractEndpointProber(
                instanceRegistry, ActuatorEndpoints.SET_FOR_LOGGER_GROUP, tokenProvider, httpClient);
    }

    @Bean
    public ProxyingEndpointProber getAllLoggersEndpointProber() {
        return new ProxyingEndpointProber(
                instanceRegistry, ActuatorEndpoints.GET_ALL_LOGGERS, tokenProvider, httpClient);
    }

    @Bean
    public ProxyingEndpointProber getOneLoggerEndpointProber() {
        return new ProxyingEndpointProber(
                instanceRegistry, ActuatorEndpoints.GET_ONE_LOGGER, tokenProvider, httpClient);
    }

    @Bean
    public ProxyingEndpointProber getLoggerGroupEndpointProber() {
        return new ProxyingEndpointProber(
                instanceRegistry, ActuatorEndpoints.GET_LOGGER_GROUP, tokenProvider, httpClient);
    }

    // Caches
    @Bean
    public DiscardingAbstractEndpointProber clearAllCachesEndpointProber() {
        return new DiscardingAbstractEndpointProber(
                instanceRegistry, ActuatorEndpoints.CLEAR_ALL_CACHES, tokenProvider, httpClient);
    }

    @Bean
    public DiscardingAbstractEndpointProber clearSingleCacheEndpointProber() {
        return new DiscardingAbstractEndpointProber(
                instanceRegistry, ActuatorEndpoints.CLEAR_SINGLE_CACHE, tokenProvider, httpClient);
    }

    @Bean
    public DiscardingAbstractEndpointProber enableCacheEndpointProver() {
        return new DiscardingAbstractEndpointProber(
                instanceRegistry, ActuatorEndpoints.ENABLE_CACHE, tokenProvider, httpClient);
    }

    @Bean
    public DiscardingAbstractEndpointProber disableCacheEndpointProver() {
        return new DiscardingAbstractEndpointProber(
                instanceRegistry, ActuatorEndpoints.DISABLE_CACHE, tokenProvider, httpClient);
    }

    @Bean
    public ProxyingEndpointProber getSingleCacheEndpointProver() {
        return new ProxyingEndpointProber(
                instanceRegistry, ActuatorEndpoints.GET_SINGLE_CACHE, tokenProvider, httpClient);
    }

    @Bean
    public ProxyingEndpointProber getAllCachesEndpointProver() {
        return new ProxyingEndpointProber(
                instanceRegistry, ActuatorEndpoints.GET_ALL_CACHES, tokenProvider, httpClient);
    }

    // Details
    @Bean
    public DefaultEndpointProber<InstanceDetails> getDetailsEndpointProber(ObjectMapper objectMapper) {
        return new DefaultEndpointProber<>(
                instanceRegistry,
                new DefaultJacksonMessageDeserializationStrategy<>(objectMapper, InstanceDetails.class),
                tokenProvider,
                ActuatorEndpoints.GET_DETAILS,
                httpClient);
    }

    // Metadata
    @Bean
    public DefaultEndpointProber<BasicRegistrationMetadata> metadataEndpointProber(ObjectMapper objectMapper) {
        return new DefaultEndpointProber<>(
                instanceRegistry,
                new DefaultJacksonMessageDeserializationStrategy<>(objectMapper, BasicRegistrationMetadata.class),
                tokenProvider,
                ActuatorEndpoints.METADATA,
                httpClient);
    }

    // Ping
    @Bean
    public DefaultEndpointProber<MasterPing> masterPingEndpointProber(ObjectMapper objectMapper) {
        return new DefaultEndpointProber<>(
                instanceRegistry,
                new DefaultJacksonMessageDeserializationStrategy<>(objectMapper, MasterPing.class),
                tokenProvider,
                ActuatorEndpoints.PING,
                httpClient);
    }

    // Beans
    @Bean
    public EndpointProber<byte[]> getBeansEndpointProber() {
        return new CachingEndpointProber<>(
                new ProxyingEndpointProber(instanceRegistry, ActuatorEndpoints.GET_BEANS, tokenProvider, httpClient),
                bytes -> Arrays.copyOf(bytes, bytes.length));
    }

    // ThreadDump
    @Bean
    public ProxyingEndpointProber getThreadDumpEndpointProber() {
        return new ProxyingEndpointProber(
                instanceRegistry, ActuatorEndpoints.GET_THREAD_DUMP, tokenProvider, httpClient);
    }

    @Bean
    public DiscardingAbstractEndpointProber enableThreadDumpEndpointProber() {
        return new DiscardingAbstractEndpointProber(
                instanceRegistry,
                ActuatorEndpoints.THREAD_DUMP_ENABLE_CONTENTION_MONITORING,
                tokenProvider,
                httpClient);
    }

    @Bean
    public DiscardingAbstractEndpointProber disableThreadDumpEndpointProber() {
        return new DiscardingAbstractEndpointProber(
                instanceRegistry,
                ActuatorEndpoints.THREAD_DUMP_DISABLE_CONTENTION_MONITORING,
                tokenProvider,
                httpClient);
    }

    // Metrics
    @Bean
    public ProxyingEndpointProber getMetricGroupsEndpointProver() {
        return new ProxyingEndpointProber(
                instanceRegistry, ActuatorEndpoints.GET_METRIC_GROUPS, tokenProvider, httpClient);
    }

    @Bean
    public ProxyingEndpointProber getSingleMetricEndpointProver() {
        return new ProxyingEndpointProber(
                instanceRegistry, ActuatorEndpoints.GET_SINGLE_METRIC, tokenProvider, httpClient);
    }

    // Environment Property
    @Bean
    public EndpointProber<EnvironmentFeed> getAllEnvironmentEndpointProver(ObjectMapper objectMapper) {
        return new DefaultEndpointProber<>(
                instanceRegistry,
                new DefaultJacksonMessageDeserializationStrategy<>(objectMapper, EnvironmentFeed.class),
                tokenProvider,
                ActuatorEndpoints.GET_ALL_ENV_PROPERTIES,
                httpClient);
    }

    // HeapDump
    @Bean
    public DefaultEndpointProber<Resource> getHeapDumpEndpointProver(
            HeapDumpMessageDeserializationStrategy deserializationStrategy) {
        return new DefaultEndpointProber<>(
                instanceRegistry, deserializationStrategy, tokenProvider, ActuatorEndpoints.GET_HEAP_DUMP, httpClient);
    }

    // Garbage Collector Log
    @Bean
    public DefaultEndpointProber<Resource> getGcLogFileEndpointProber(
            GcLogFileMessageDeserializationStrategy deserializationStrategy) {
        return new DefaultEndpointProber<>(
                instanceRegistry,
                deserializationStrategy,
                tokenProvider,
                ActuatorEndpoints.GET_GC_LOG_FILE,
                httpClient);
    }

    @Bean
    public ProxyingEndpointProber getGcLogStatusEndpointProber() {
        return new ProxyingEndpointProber(
                instanceRegistry, ActuatorEndpoints.GET_STATUS_GC_LOGGING, tokenProvider, httpClient);
    }

    @Bean
    public DiscardingAbstractEndpointProber gcTriggerEndpointProber() {
        return new DiscardingAbstractEndpointProber(
                instanceRegistry, ActuatorEndpoints.GC_TRIGGER, tokenProvider, httpClient);
    }

    @Bean
    public DiscardingAbstractEndpointProber enableGcLoggingEndpointProber() {
        return new DiscardingAbstractEndpointProber(
                instanceRegistry, ActuatorEndpoints.ENABLE_GC_LOGGING, tokenProvider, httpClient);
    }

    @Bean
    public DiscardingAbstractEndpointProber disableGcLoggingEndpointProber() {
        return new DiscardingAbstractEndpointProber(
                instanceRegistry, ActuatorEndpoints.DISABLE_GC_LOGGING, tokenProvider, httpClient);
    }

    // Scheduled tasks
    @Bean
    public ProxyingEndpointProber getScheduledTasksEndpointProber() {
        return new ProxyingEndpointProber(
                instanceRegistry, ActuatorEndpoints.GET_SCHEDULED_TASKS, tokenProvider, httpClient);
    }

    @Bean
    public DiscardingAbstractEndpointProber executeScheduledTasksEndpointProber() {
        return new DiscardingAbstractEndpointProber(
                instanceRegistry, ActuatorEndpoints.EXECUTE_SCHEDULED_TASK, tokenProvider, httpClient);
    }

    @Bean
    public DiscardingAbstractEndpointProber enableScheduledTasksEndpointProber() {
        return new DiscardingAbstractEndpointProber(
                instanceRegistry, ActuatorEndpoints.ENABLE_SCHEDULED_TASK, tokenProvider, httpClient);
    }

    @Bean
    public DiscardingAbstractEndpointProber disableScheduledTasksEndpointProber() {
        return new DiscardingAbstractEndpointProber(
                instanceRegistry, ActuatorEndpoints.DISABLE_SCHEDULED_TASK, tokenProvider, httpClient);
    }

    @Bean
    public DiscardingAbstractEndpointProber modifyCronExpressionScheduledTasksEndpointProber() {
        return new DiscardingAbstractEndpointProber(
                instanceRegistry, ActuatorEndpoints.MODIFY_CRON_EXPRESSION_SCHEDULED_TASK, tokenProvider, httpClient);
    }

    @Bean
    public DiscardingAbstractEndpointProber modifyIntervalScheduledTasksEndpointProber() {
        return new DiscardingAbstractEndpointProber(
                instanceRegistry, ActuatorEndpoints.MODIFY_INTERVAL_SCHEDULED_TASK, tokenProvider, httpClient);
    }

    // Conditions
    @Bean
    public EndpointProber<byte[]> getConditionsProber() {
        return new CachingEndpointProber<>(
                new ProxyingEndpointProber(
                        instanceRegistry, ActuatorEndpoints.GET_CONDITIONS, tokenProvider, httpClient),
                bytes -> Arrays.copyOf(bytes, bytes.length));
    }

    // Configuration Properties
    @Bean
    public EndpointProber<ConfigurationPropertiesFeed> getConfigPropsProber(ObjectMapper objectMapper) {
        return new DefaultEndpointProber<>(
                instanceRegistry,
                new DefaultJacksonMessageDeserializationStrategy<>(objectMapper, ConfigurationPropertiesFeed.class),
                tokenProvider,
                ActuatorEndpoints.GET_CONFIG_PROPS,
                httpClient);
    }

    // Dependencies (SBOM). To be removed in 1.6 together with ActuatorEndpoints.GET_DEPENDENCIES_SBOM.
    @Bean
    @Deprecated(forRemoval = true, since = "1.3")
    public EndpointProber<byte[]> getDependenciesSbomProber() {
        return new CachingEndpointProber<>(new ProxyingEndpointProber(
                instanceRegistry, ActuatorEndpoints.GET_DEPENDENCIES_SBOM, tokenProvider, httpClient));
    }
}
