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
package com.axelixlabs.axelix.sbs.spring.transactions;

import org.springframework.boot.actuate.endpoint.annotation.DeleteOperation;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;

import com.axelixlabs.axelix.common.api.TransactionMonitoringFeed;

/**
 * Custom Spring Boot Actuator endpoint for transaction monitoring.
 *
 * <p>Exposes real-time transaction execution statistics.
 *
 * @since 22.01.2026
 * @author Nikita Kirillov
 */
@Endpoint(id = "axelix-transactions-monitoring")
public class TransactionMonitoringEndpoint {

    private final TransactionMonitoringService transactionMonitoringService;

    public TransactionMonitoringEndpoint(TransactionMonitoringService transactionMonitoringService) {
        this.transactionMonitoringService = transactionMonitoringService;
    }

    @ReadOperation
    public TransactionMonitoringFeed getTransactionStats() {
        return transactionMonitoringService.getMonitoringFeed();
    }

    @DeleteOperation
    public void clearTransactionStats() {
        transactionMonitoringService.clearAllStats();
    }
}
