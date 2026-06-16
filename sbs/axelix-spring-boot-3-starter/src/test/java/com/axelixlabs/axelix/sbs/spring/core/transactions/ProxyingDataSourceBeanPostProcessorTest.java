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
package com.axelixlabs.axelix.sbs.spring.core.transactions;

import javax.sql.DataSource;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test for {@link ProxyingDataSourceBeanPostProcessor}.
 *
 * @author Sergey Cherkasov
 * @author Artemiy Degtyarev
 */
class ProxyingDataSourceBeanPostProcessorTest extends AbstractTransactionMonitoringIntegrationTest {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private ProxyingDataSourceBeanPostProcessor subject;

    @Test
    void shouldWrapDataSourceWithProxyingDataSource() {
        assertThat(dataSource).isInstanceOf(ProxyingDataSource.class);
    }

    @Test
    void shouldNotWrapNonDataSourceBean() {
        Object nonDataSourceBean = new Object();

        Object result = subject.postProcessAfterInitialization(nonDataSourceBean, "someBean");
        assertThat(result).isSameAs(nonDataSourceBean);
    }

    @Test
    void shouldNotDoubleWrapAlreadyProxiedDataSource() {
        ProxyingDataSource alreadyProxied = (ProxyingDataSource) dataSource;

        Object result = subject.postProcessAfterInitialization(alreadyProxied, "dataSource");

        assertThat(result).isSameAs(alreadyProxied);
    }
}
