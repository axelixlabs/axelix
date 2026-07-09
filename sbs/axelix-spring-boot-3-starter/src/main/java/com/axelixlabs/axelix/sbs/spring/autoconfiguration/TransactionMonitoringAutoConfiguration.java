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
package com.axelixlabs.axelix.sbs.spring.autoconfiguration;

import java.util.List;

import jakarta.servlet.DispatcherType;

import org.hibernate.jpa.boot.spi.IntegratorProvider;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.actuate.autoconfigure.endpoint.condition.ConditionalOnAvailableEndpoint;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.orm.jpa.HibernatePropertiesCustomizer;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.core.Ordered;

import com.axelixlabs.axelix.sbs.spring.core.config.TransactionMonitoringConfigurationProperties;
import com.axelixlabs.axelix.sbs.spring.core.metrics.AxelixMetricsPublisher;
import com.axelixlabs.axelix.sbs.spring.core.metrics.AxelixMetricsPublisherAutoConfiguration;
import com.axelixlabs.axelix.sbs.spring.core.persistence.ProxyingDataSourceBeanPostProcessor;
import com.axelixlabs.axelix.sbs.spring.core.persistence.TransactionMonitoringBeanPostProcessor;
import com.axelixlabs.axelix.sbs.spring.core.persistence.TransactionMonitoringEndpoint;
import com.axelixlabs.axelix.sbs.spring.core.persistence.TranssactionStackCleanupFilter;
import com.axelixlabs.axelix.sbs.spring.core.persistence.hibernate.ConditionalOnHibernateActive;
import com.axelixlabs.axelix.sbs.spring.core.persistence.hibernate.NPlusOneCollectionLoadListener;
import com.axelixlabs.axelix.sbs.spring.core.persistence.hibernate.NPlusOneEntityLoadListener;
import com.axelixlabs.axelix.sbs.spring.core.persistence.hibernate.NPlusOneIntegrator;
import com.axelixlabs.axelix.sbs.spring.core.persistence.hibernate.pagination.ConditionalOnLoggingSystem;
import com.axelixlabs.axelix.sbs.spring.core.persistence.hibernate.pagination.Log4j2InMemoryPaginationAppenderRegistrar;
import com.axelixlabs.axelix.sbs.spring.core.persistence.hibernate.pagination.LogbackInMemoryPaginationAppenderRegistrar;
import com.axelixlabs.axelix.sbs.spring.core.persistence.transaction.DefaultTransactionMonitoringService;
import com.axelixlabs.axelix.sbs.spring.core.persistence.transaction.DefaultTransactionStatsCollector;
import com.axelixlabs.axelix.sbs.spring.core.persistence.transaction.TransactionAccessor;
import com.axelixlabs.axelix.sbs.spring.core.persistence.transaction.TransactionMonitoringService;
import com.axelixlabs.axelix.sbs.spring.core.persistence.transaction.TransactionStatsCollector;

import static org.hibernate.jpa.boot.internal.EntityManagerFactoryBuilderImpl.INTEGRATOR_PROVIDER;

/**
 * Auto-configuration for Transaction Monitoring infrastructure.
 *
 * @since 21.01.2026
 * @author Nikita Kirillov
 * @author Sergey Cherkasov
 * @author Ilya Naumov
 * @author Vyacheslav Yanin
 */
@AutoConfiguration(after = {AxelixMetricsPublisherAutoConfiguration.class, ValidationListenerAutoConfiguration.class})
@ConditionalOnAvailableEndpoint(endpoint = TransactionMonitoringEndpoint.class)
public class TransactionMonitoringAutoConfiguration {

    @Bean
    @ConfigurationProperties(prefix = TransactionMonitoringConfigurationProperties.CONFIG_PROPS_PREFIX)
    public TransactionMonitoringConfigurationProperties transactionMonitoringConfigurationProperties() {
        return new TransactionMonitoringConfigurationProperties();
    }

    @Bean
    @ConditionalOnMissingBean
    public TransactionStatsCollector transactionStatsCollector(
            TransactionMonitoringConfigurationProperties properties) {

        return new DefaultTransactionStatsCollector(properties.getMaxTransactionsPerMethod());
    }

    @Bean
    @ConditionalOnMissingBean
    public TransactionMonitoringService transactionMonitoringService(
            TransactionStatsCollector transactionStatsCollector) {
        return new DefaultTransactionMonitoringService(transactionStatsCollector);
    }

    @Bean
    @ConditionalOnMissingBean
    public TransactionMonitoringEndpoint transactionMonitoringEndpoint(
            TransactionMonitoringService transactionMonitoringService) {
        return new TransactionMonitoringEndpoint(transactionMonitoringService);
    }

    @Bean
    @ConditionalOnMissingBean
    public TransactionMonitoringBeanPostProcessor transactionMonitoringBeanPostProcessor(
            TransactionStatsCollector transactionStatsCollector,
            TransactionAccessor transactionAccessor,
            ObjectProvider<AxelixMetricsPublisher> metricsPublisherObjectProvider) {
        return new TransactionMonitoringBeanPostProcessor(
                transactionStatsCollector, metricsPublisherObjectProvider, transactionAccessor);
    }

    @Bean
    @ConditionalOnMissingBean
    public ProxyingDataSourceBeanPostProcessor transactionMonitoringDataSourceBeanPostProcessor(
            TransactionAccessor transactionAccessor) {
        return new ProxyingDataSourceBeanPostProcessor(transactionAccessor);
    }

    @Bean
    public TransactionAccessor transactionAccessor() {
        return new TransactionAccessor();
    }

    @Bean
    @ConditionalOnMissingBean
    public FilterRegistrationBean<TranssactionStackCleanupFilter> nPlusOneHolderCleanupFilterRegistration(
            TransactionAccessor transactionAccessor) {
        FilterRegistrationBean<TranssactionStackCleanupFilter> registrationBean = new FilterRegistrationBean<>();

        registrationBean.setFilter(new TranssactionStackCleanupFilter(transactionAccessor));
        registrationBean.addUrlPatterns("/*");
        registrationBean.setOrder(Ordered.LOWEST_PRECEDENCE);

        registrationBean.setDispatcherTypes(DispatcherType.REQUEST, DispatcherType.ASYNC, DispatcherType.ERROR);

        return registrationBean;
    }

    @Configuration
    @ConditionalOnHibernateActive
    static class HibernateRelatedConfiguration {

        @Bean
        @ConditionalOnMissingBean
        public HibernatePropertiesCustomizer axelixhibernatePropertiesCustomizer(
                TransactionAccessor transactionAccessor) {
            return properties ->
                    properties.put(INTEGRATOR_PROVIDER, (IntegratorProvider) () -> List.of(new NPlusOneIntegrator(
                            new NPlusOneEntityLoadListener(transactionAccessor),
                            new NPlusOneCollectionLoadListener(transactionAccessor))));
        }
    }

    @Configuration
    @ConditionalOnHibernateActive
    @ConditionalOnLoggingSystem(ConditionalOnLoggingSystem.System.LOGBACK)
    @ConditionalOnProperty(
            prefix = "axelix.sbs.transaction.monitoring.in-memory-pagination-detection",
            name = "enabled",
            havingValue = "true",
            matchIfMissing = true)
    static class LogbackInMemoryPaginationAppenderConfiguration {

        @EventListener(ApplicationReadyEvent.class)
        public void registerAppender() {
            new LogbackInMemoryPaginationAppenderRegistrar().register();
        }
    }

    @Configuration
    @ConditionalOnHibernateActive
    @ConditionalOnLoggingSystem(ConditionalOnLoggingSystem.System.LOG4J2)
    @ConditionalOnProperty(
            prefix = "axelix.sbs.transaction.monitoring.in-memory-pagination-detection",
            name = "enabled",
            havingValue = "true",
            matchIfMissing = true)
    static class Log4j2InMemoryPaginationAppenderConfiguration {

        @EventListener(ApplicationReadyEvent.class)
        public void registerAppender() {
            new Log4j2InMemoryPaginationAppenderRegistrar().register();
        }
    }
}
