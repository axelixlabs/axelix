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

import org.jspecify.annotations.NonNull;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;

/**
 * {@link BeanPostProcessor} that wraps {@link DataSource} beans with a monitoring
 * {@link DelegatingDataSource} to collect real-time SQL query execution statistics.
 *
 * @author Sergey Cherkasov
 */
public final class TransactionMonitoringDataSourceBeanPostProcessor implements BeanPostProcessor {

    private final QueriesStatsCollector queriesStatsCollector;

    public TransactionMonitoringDataSourceBeanPostProcessor(QueriesStatsCollector queriesStatsCollector) {
        this.queriesStatsCollector = queriesStatsCollector;
    }

    @Override
    public Object postProcessAfterInitialization(@NonNull Object bean, @NonNull String beanName) throws BeansException {
        if (!(bean instanceof DataSource) || bean instanceof DelegatingDataSource) {
            return bean;
        }

        return new DelegatingDataSource((DataSource) bean, queriesStatsCollector);
    }
}
