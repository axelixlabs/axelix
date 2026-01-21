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
package com.nucleonforge.axelix.master.autoconfiguration.probers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;

import com.nucleonforge.axelix.common.api.BeansFeed;
import com.nucleonforge.axelix.common.api.InstanceDetails;
import com.nucleonforge.axelix.common.api.ThreadDumpFeed;
import com.nucleonforge.axelix.common.api.caches.CachesFeed;
import com.nucleonforge.axelix.common.api.caches.SingleCache;
import com.nucleonforge.axelix.common.api.loggers.LoggerGroup;
import com.nucleonforge.axelix.common.api.loggers.LoggerLevels;
import com.nucleonforge.axelix.common.api.loggers.ServiceLoggers;
import com.nucleonforge.axelix.common.api.metrics.MetricProfile;
import com.nucleonforge.axelix.common.api.metrics.MetricsGroupsFeed;
import com.nucleonforge.axelix.common.domain.spring.actuator.ActuatorEndpoints;
import com.nucleonforge.axelix.master.service.serde.BeansJacksonMessageDeserializationStrategy;
import com.nucleonforge.axelix.master.service.serde.DetailsJacksonMessageDeserializationStrategy;
import com.nucleonforge.axelix.master.service.serde.ThreadDumpJacksonMessageDeserializationStrategy;
import com.nucleonforge.axelix.master.service.serde.caches.ServiceCachesJacksonMessageDeserializationStrategy;
import com.nucleonforge.axelix.master.service.serde.caches.SingleCacheJacksonMessageDeserializationStrategy;
import com.nucleonforge.axelix.master.service.serde.loggers.LoggerGroupJacksonMessageDeserializationStrategy;
import com.nucleonforge.axelix.master.service.serde.loggers.LoggerLevelsJacksonMessageDeserializationStrategy;
import com.nucleonforge.axelix.master.service.serde.loggers.ServiceLoggersJacksonMessageDeserializationStrategy;
import com.nucleonforge.axelix.master.service.serde.metrics.MetricsGroupsJacksonDeserializationStrategy;
import com.nucleonforge.axelix.master.service.serde.metrics.SingleMetricJacksonDeserializationStrategy;
import com.nucleonforge.axelix.master.service.state.InstanceRegistry;
import com.nucleonforge.axelix.master.service.transport.DefaultEndpointProber;
import com.nucleonforge.axelix.master.service.transport.DiscardingAbstractEndpointProber;
import com.nucleonforge.axelix.master.service.transport.EndpointProber;

/**
 * Configuration that creates necessary {@link EndpointProber} instances to
 * access the API on the managed service side.
 *
 * @author Mikhail Polivakha
 * @author Sergey Cherkasov
 */
// TODO: We should dynamically register instances of EndpointProbers.
//  We can do that, but that requires a significant ActuatorEndpoint revisiting.
//  In particular, ActuatorEndpoint should now not only the request Http Path and Http Method,
//  but it should also know the shape of the returned object, along with it's format.
@AutoConfiguration
public class EndpointProbersAutoConfiguration {

    @Autowired
    private InstanceRegistry instanceRegistry;

    // Loggers
    @Bean
    public DiscardingAbstractEndpointProber setOneLoggerEndpointProber() {
        return new DiscardingAbstractEndpointProber(instanceRegistry, ActuatorEndpoints.SET_ONE_LOGGER);
    }

    @Bean
    public DiscardingAbstractEndpointProber clearLevelForLoggerEndpointProber() {
        return new DiscardingAbstractEndpointProber(instanceRegistry, ActuatorEndpoints.CLEAR_FOR_LOGGER);
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
    public DefaultEndpointProber<SingleCache> getSingleCacheEndpointProver(
            SingleCacheJacksonMessageDeserializationStrategy deserializationStrategy) {
        return new DefaultEndpointProber<>(
                instanceRegistry, deserializationStrategy, ActuatorEndpoints.GET_SINGLE_CACHE);
    }

    @Bean
    public DefaultEndpointProber<CachesFeed> getAllCachesEndpointProver(
            ServiceCachesJacksonMessageDeserializationStrategy deserializationStrategy) {
        return new DefaultEndpointProber<>(instanceRegistry, deserializationStrategy, ActuatorEndpoints.GET_ALL_CACHES);
    }

    // Details
    @Bean
    public DefaultEndpointProber<InstanceDetails> getDetailsEndpointProber(
            DetailsJacksonMessageDeserializationStrategy deserializationStrategy) {
        return new DefaultEndpointProber<>(instanceRegistry, deserializationStrategy, ActuatorEndpoints.GET_DETAILS);
    }

    // Beans
    @Bean
    public DefaultEndpointProber<BeansFeed> getBeansEndpointProber(
            BeansJacksonMessageDeserializationStrategy deserializationStrategy) {
        return new DefaultEndpointProber<>(instanceRegistry, deserializationStrategy, ActuatorEndpoints.GET_BEANS);
    }

    // ThreadDump
    @Bean
    public DefaultEndpointProber<ThreadDumpFeed> getThreadDumpEndpointProber(
            ThreadDumpJacksonMessageDeserializationStrategy deserializationStrategy) {
        return new DefaultEndpointProber<>(
                instanceRegistry, deserializationStrategy, ActuatorEndpoints.GET_THREAD_DUMP);
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
    public DefaultEndpointProber<MetricsGroupsFeed> getMetricGroupsEndpointProver(
            MetricsGroupsJacksonDeserializationStrategy deserializationStrategy) {
        return new DefaultEndpointProber<>(
                instanceRegistry, deserializationStrategy, ActuatorEndpoints.GET_METRIC_GROUPS);
    }

    @Bean
    public DefaultEndpointProber<MetricProfile> getSingleMetricEndpointProver(
            SingleMetricJacksonDeserializationStrategy deserializationStrategy) {
        return new DefaultEndpointProber<>(
                instanceRegistry, deserializationStrategy, ActuatorEndpoints.GET_SINGLE_METRIC);
    }
}
