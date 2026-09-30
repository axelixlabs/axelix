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
package com.axelixlabs.axelix.sbs.spring.core.persistence;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.junit.jupiter.api.Test;

import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

import com.axelixlabs.axelix.sbs.spring.core.persistence.transaction.TransactionAccessor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

/**
 * Integration test for {@link ProxyingDataSourceBeanPostProcessor}.
 *
 * @author Sergey Cherkasov
 * @author Artemiy Degtyarev
 * @author Nikita Kirillov
 */
class ProxyingDataSourceBeanPostProcessorTest extends AbstractTransactionMonitoringSharedContextTest {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private ProxyingDataSourceBeanPostProcessor subject;

    @Test
    void shouldWrapDataSourceWithMonitoringProxy() throws SQLException {
        assertThat(AopUtils.isCglibProxy(dataSource)).isTrue();

        try (Connection connection = dataSource.getConnection()) {
            assertThat(connection).isInstanceOf(ProxyingConnection.class);
        }
    }

    @Test
    void shouldNotWrapNonDataSourceBean() {
        Object nonDataSourceBean = new Object();

        Object result = subject.postProcessAfterInitialization(nonDataSourceBean, "someBean");
        assertThat(result).isSameAs(nonDataSourceBean);
    }

    @Test
    void shouldNotWrapDataSourceWithFinalMethod() {
        Object result = subject.postProcessAfterInitialization(new FinalMethodDataSource(), "finalMethodDataSource");

        assertThat(AopUtils.isAopProxy(result)).isFalse();
    }

    static class FinalMethodDataSource extends DriverManagerDataSource {
        FinalMethodDataSource() {
            super("jdbc:h2:mem:final-method-test;DB_CLOSE_DELAY=-1");
        }

        @Override
        public final Connection getConnection() throws SQLException {
            return super.getConnection();
        }
    }

    static class RoutingDataSourceTest {

        @Test
        void staysAssignmentCompatibleAcrossInClassBeanMethodCall() {
            assertThatCode(() -> {
                        try (AnnotationConfigApplicationContext context =
                                new AnnotationConfigApplicationContext(RoutingTestConfig.class)) {
                            AbstractRoutingDataSource routingDataSource =
                                    context.getBean(AbstractRoutingDataSource.class);
                            DataSourceConsumer consumer = context.getBean(DataSourceConsumer.class);

                            assertThat(consumer.dataSource).isSameAs(routingDataSource);
                        }
                    })
                    .doesNotThrowAnyException();
        }

        static class SingleTargetRoutingDataSource extends AbstractRoutingDataSource {
            @Override
            protected Object determineCurrentLookupKey() {
                return "default";
            }
        }

        static class DataSourceConsumer {
            final AbstractRoutingDataSource dataSource;

            DataSourceConsumer(AbstractRoutingDataSource dataSource) {
                this.dataSource = dataSource;
            }
        }

        @TestConfiguration
        static class RoutingTestConfig {

            @Bean
            public TransactionAccessor transactionAccessor() {
                return new TransactionAccessor();
            }

            @Bean
            public ProxyingDataSourceBeanPostProcessor proxyingDataSourceBeanPostProcessor(
                    TransactionAccessor transactionAccessor) {
                return new ProxyingDataSourceBeanPostProcessor(transactionAccessor);
            }

            @Bean
            public AbstractRoutingDataSource routingDataSource() {
                SingleTargetRoutingDataSource routing = new SingleTargetRoutingDataSource();
                DataSource target = new DriverManagerDataSource("jdbc:h2:mem:routing-test;DB_CLOSE_DELAY=-1");

                Map<Object, Object> targets = new HashMap<>();
                targets.put("default", target);
                routing.setTargetDataSources(targets);
                routing.setDefaultTargetDataSource(target);
                routing.afterPropertiesSet();

                return routing;
            }

            @Bean
            public DataSourceConsumer dataSourceConsumer() {
                return new DataSourceConsumer(routingDataSource());
            }
        }
    }
}
