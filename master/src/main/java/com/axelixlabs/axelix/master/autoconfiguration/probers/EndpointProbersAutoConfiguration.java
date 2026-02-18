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

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.Resource;

import com.axelixlabs.axelix.common.api.InstanceDetails;
import com.axelixlabs.axelix.common.api.ProfileMutationResult;
import com.axelixlabs.axelix.common.api.loggers.LoggerGroup;
import com.axelixlabs.axelix.common.api.loggers.LoggerLevels;
import com.axelixlabs.axelix.common.api.loggers.ServiceLoggers;
import com.axelixlabs.axelix.master.domain.ActuatorEndpoints;
import com.axelixlabs.axelix.master.service.serde.DetailsJacksonMessageDeserializationStrategy;
import com.axelixlabs.axelix.master.service.serde.GcLogFileMessageDeserializationStrategy;
import com.axelixlabs.axelix.master.service.serde.HeapDumpMessageDeserializationStrategy;
import com.axelixlabs.axelix.master.service.serde.ProfileMutationJacksonMessageDeserializationStrategy;
import com.axelixlabs.axelix.master.service.serde.loggers.LoggerGroupJacksonMessageDeserializationStrategy;
import com.axelixlabs.axelix.master.service.serde.loggers.LoggerLevelsJacksonMessageDeserializationStrategy;
import com.axelixlabs.axelix.master.service.serde.loggers.ServiceLoggersJacksonMessageDeserializationStrategy;
import com.axelixlabs.axelix.master.service.state.InstanceRegistry;
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
 */
// TODO: We should dynamically register instances of EndpointProbers.
//  We can do that, but that requires a significant ActuatorEndpoint revisiting.
//  In particular, ActuatorEndpoint should now not only the request Http Path and Http Method,
//  but it should also know the shape of the returned object, along with it's format.
@AutoConfiguration
public class EndpointProbersAutoConfiguration {

    private final InstanceRegistry instanceRegistry;

    public EndpointProbersAutoConfiguration(InstanceRegistry instanceRegistry) {
        this.instanceRegistry = instanceRegistry;
    }

    // Loggers
    @Bean
    public DiscardingAbstractEndpointProber setOneLoggerEndpointProber() {
        return new DiscardingAbstractEndpointProber(instanceRegistry, ActuatorEndpoints.SET_ONE_LOGGER);
    }

    @Bean
    public DiscardingAbstractEndpointProber resetLoggerLevelForLoggerEndpointProber() {
        return new DiscardingAbstractEndpointProber(instanceRegistry, ActuatorEndpoints.RESET_FOR_LOGGER);
    }

    @Bean
    public DiscardingAbstractEndpointProber setLevelForLoggerGroupEndpointProber() {
        return new DiscardingAbstractEndpointProber(instanceRegistry, ActuatorEndpoints.SET_FOR_LOGGER_GROUP);
    }

    @Bean
    public DefaultEndpointProber<ServiceLoggers> getAllLoggersEndpointProber(
            ServiceLoggersJacksonMessageDeserializationStrategy deserializationStrategy) {
        return new DefaultEndpointProber<>(
                instanceRegistry, deserializationStrategy, ActuatorEndpoints.GET_ALL_LOGGERS);
    }

    @Bean
    public DefaultEndpointProber<LoggerLevels> getOneLoggerEndpointProber(
            LoggerLevelsJacksonMessageDeserializationStrategy deserializationStrategy) {
        return new DefaultEndpointProber<>(instanceRegistry, deserializationStrategy, ActuatorEndpoints.GET_ONE_LOGGER);
    }

    @Bean
    public DefaultEndpointProber<LoggerGroup> getLoggerGroupEndpointProber(
            LoggerGroupJacksonMessageDeserializationStrategy deserializationStrategy) {
        return new DefaultEndpointProber<>(
                instanceRegistry, deserializationStrategy, ActuatorEndpoints.GET_LOGGER_GROUP);
    }

    // Caches
    @Bean
    public DiscardingAbstractEndpointProber clearAllCachesEndpointProber() {
        return new DiscardingAbstractEndpointProber(instanceRegistry, ActuatorEndpoints.CLEAR_ALL_CACHES);
    }

    @Bean
    public DiscardingAbstractEndpointProber clearSingleCacheEndpointProber() {
        return new DiscardingAbstractEndpointProber(instanceRegistry, ActuatorEndpoints.CLEAR_SINGLE_CACHE);
    }

    @Bean
    public DiscardingAbstractEndpointProber enableCacheEndpointProver() {
        return new DiscardingAbstractEndpointProber(instanceRegistry, ActuatorEndpoints.ENABLE_CACHE);
    }

    @Bean
    public DiscardingAbstractEndpointProber disableCacheEndpointProver() {
        return new DiscardingAbstractEndpointProber(instanceRegistry, ActuatorEndpoints.DISABLE_CACHE);
    }

    @Bean
    public DiscardingAbstractEndpointProber disableCacheManagerEndpointProver() {
        return new DiscardingAbstractEndpointProber(instanceRegistry, ActuatorEndpoints.DISABLE_CACHES_MANAGER);
    }

    @Bean
    public DiscardingAbstractEndpointProber enableCacheManagerEndpointProver() {
        return new DiscardingAbstractEndpointProber(instanceRegistry, ActuatorEndpoints.ENABLE_CACHE_MANAGER);
    }

    @Bean
    public ProxyingEndpointProber getSingleCacheEndpointProver() {
        return new ProxyingEndpointProber(instanceRegistry, ActuatorEndpoints.GET_SINGLE_CACHE);
    }

    @Bean
    public ProxyingEndpointProber getAllCachesEndpointProver() {
        return new ProxyingEndpointProber(instanceRegistry, ActuatorEndpoints.GET_ALL_CACHES);
    }

    // Details
    @Bean
    public DefaultEndpointProber<InstanceDetails> getDetailsEndpointProber(
            DetailsJacksonMessageDeserializationStrategy deserializationStrategy) {
        return new DefaultEndpointProber<>(instanceRegistry, deserializationStrategy, ActuatorEndpoints.GET_DETAILS);
    }

    // Beans
    @Bean
    public ProxyingEndpointProber getBeansEndpointProber() {
        return new ProxyingEndpointProber(instanceRegistry, ActuatorEndpoints.GET_BEANS);
    }

    // ThreadDump
    @Bean
    public ProxyingEndpointProber getThreadDumpEndpointProber() {
        return new ProxyingEndpointProber(instanceRegistry, ActuatorEndpoints.GET_THREAD_DUMP);
    }

    @Bean
    public DiscardingAbstractEndpointProber enableThreadDumpEndpointProber() {
        return new DiscardingAbstractEndpointProber(
                instanceRegistry, ActuatorEndpoints.THREAD_DUMP_ENABLE_CONTENTION_MONITORING);
    }

    @Bean
    public DiscardingAbstractEndpointProber disableThreadDumpEndpointProber() {
        return new DiscardingAbstractEndpointProber(
                instanceRegistry, ActuatorEndpoints.THREAD_DUMP_DISABLE_CONTENTION_MONITORING);
    }

    // Metrics
    @Bean
    public ProxyingEndpointProber getMetricGroupsEndpointProver() {
        return new ProxyingEndpointProber(instanceRegistry, ActuatorEndpoints.GET_METRIC_GROUPS);
    }

    @Bean
    public ProxyingEndpointProber getSingleMetricEndpointProver() {
        return new ProxyingEndpointProber(instanceRegistry, ActuatorEndpoints.GET_SINGLE_METRIC);
    }

    // Environment Property
    @Bean
    public ProxyingEndpointProber getAllEnvironmentEndpointProver() {
        return new ProxyingEndpointProber(instanceRegistry, ActuatorEndpoints.GET_ALL_ENV_PROPERTIES);
    }

    // HeapDump
    @Bean
    public DefaultEndpointProber<Resource> getHeapDumpEndpointProver(
            HeapDumpMessageDeserializationStrategy deserializationStrategy) {
        return new DefaultEndpointProber<>(instanceRegistry, deserializationStrategy, ActuatorEndpoints.GET_HEAP_DUMP);
    }

    // Garbage Collector Log
    @Bean
    public DefaultEndpointProber<Resource> getGcLogFileEndpointProber(
            GcLogFileMessageDeserializationStrategy deserializationStrategy) {
        return new DefaultEndpointProber<>(
                instanceRegistry, deserializationStrategy, ActuatorEndpoints.GET_GC_LOG_FILE);
    }

    @Bean
    public ProxyingEndpointProber getGcLogStatusEndpointProber() {
        return new ProxyingEndpointProber(instanceRegistry, ActuatorEndpoints.GET_STATUS_GC_LOGGING);
    }

    @Bean
    public DiscardingAbstractEndpointProber gcTriggerEndpointProber() {
        return new DiscardingAbstractEndpointProber(instanceRegistry, ActuatorEndpoints.GC_TRIGGER);
    }

    @Bean
    public DiscardingAbstractEndpointProber enableGcLoggingEndpointProber() {
        return new DiscardingAbstractEndpointProber(instanceRegistry, ActuatorEndpoints.ENABLE_GC_LOGGING);
    }

    @Bean
    public DiscardingAbstractEndpointProber disableGcLoggingEndpointProber() {
        return new DiscardingAbstractEndpointProber(instanceRegistry, ActuatorEndpoints.DISABLE_GC_LOGGING);
    }

    // Scheduled tasks
    @Bean
    public ProxyingEndpointProber getScheduledTasksEndpointProber() {
        return new ProxyingEndpointProber(instanceRegistry, ActuatorEndpoints.GET_SCHEDULED_TASKS);
    }

    @Bean
    public DiscardingAbstractEndpointProber executeScheduledTasksEndpointProber() {
        return new DiscardingAbstractEndpointProber(instanceRegistry, ActuatorEndpoints.EXECUTE_SCHEDULED_TASK);
    }

    @Bean
    public DiscardingAbstractEndpointProber enableScheduledTasksEndpointProber() {
        return new DiscardingAbstractEndpointProber(instanceRegistry, ActuatorEndpoints.ENABLE_SCHEDULED_TASK);
    }

    @Bean
    public DiscardingAbstractEndpointProber disableScheduledTasksEndpointProber() {
        return new DiscardingAbstractEndpointProber(instanceRegistry, ActuatorEndpoints.DISABLE_SCHEDULED_TASK);
    }

    @Bean
    public DiscardingAbstractEndpointProber modifyCronExpressionScheduledTasksEndpointProber() {
        return new DiscardingAbstractEndpointProber(
                instanceRegistry, ActuatorEndpoints.MODIFY_CRON_EXPRESSION_SCHEDULED_TASK);
    }

    @Bean
    public DiscardingAbstractEndpointProber modifyIntervalScheduledTasksEndpointProber() {
        return new DiscardingAbstractEndpointProber(instanceRegistry, ActuatorEndpoints.MODIFY_INTERVAL_SCHEDULED_TASK);
    }

    // Property Management
    @Bean
    public DiscardingAbstractEndpointProber changePropertyValueEndpointProver() {
        return new DiscardingAbstractEndpointProber(instanceRegistry, ActuatorEndpoints.PROPERTY_MANAGEMENT);
    }

    // Profile Management
    @Bean
    public DefaultEndpointProber<ProfileMutationResult> replaceProfileEndpointProver(
            ProfileMutationJacksonMessageDeserializationStrategy deserializationStrategy) {
        return new DefaultEndpointProber<>(
                instanceRegistry, deserializationStrategy, ActuatorEndpoints.PROFILE_MANAGEMENT);
    }

    // Conditions
    @Bean
    public ProxyingEndpointProber getConditionsProber() {
        return new ProxyingEndpointProber(instanceRegistry, ActuatorEndpoints.GET_CONDITIONS);
    }

    // Configuration Properties
    @Bean
    public ProxyingEndpointProber getConfigPropsProber() {
        return new ProxyingEndpointProber(instanceRegistry, ActuatorEndpoints.GET_CONFIG_PROPS);
    }

    // @Transaction monitoring
    @Bean
    public ProxyingEndpointProber transactionMonitoringProxyingEndpointProper() {
        return new ProxyingEndpointProber(instanceRegistry, ActuatorEndpoints.TRANSACTION_STATS_GET);
    }

    @Bean
    public DiscardingAbstractEndpointProber transactionMonitoringDiscardingEndpointProber() {
        return new DiscardingAbstractEndpointProber(instanceRegistry, ActuatorEndpoints.TRANSACTION_STATS_CLEAR);
    }

    // Feign Client
    @Bean
    public ProxyingEndpointProber getFeignClientEndpointProber() {
        return new ProxyingEndpointProber(instanceRegistry, ActuatorEndpoints.GET_FEIGN_CLIENT);
    }
}
