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
package com.axelixlabs.axelix.sbs.spring.core.config;

import java.time.Duration;
import java.util.Objects;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for transaction monitoring feature.
 *
 * @since 26.01.2026
 * @author Nikita Kirillov
 */
@ConfigurationProperties(prefix = "axelix.sbs.transaction.monitoring")
public final class TransactionMonitoringConfigurationProperties {

    private final Integer maxTransactionsPerMethod;
    private final Duration cleanupInterval;

    /**
     * @param maxTransactionsPerMethod maximum number of transaction records to keep per method.
     * @param cleanupInterval          interval for clearing old transaction records.
     */
    public TransactionMonitoringConfigurationProperties(Integer maxTransactionsPerMethod, Duration cleanupInterval) {
        if (maxTransactionsPerMethod == null) {
            maxTransactionsPerMethod = 30;
        }

        if (cleanupInterval == null) {
            cleanupInterval = Duration.ofSeconds(5);
        }
        this.maxTransactionsPerMethod = maxTransactionsPerMethod;
        this.cleanupInterval = cleanupInterval;
    }

    public Integer maxTransactionsPerMethod() {
        return maxTransactionsPerMethod;
    }

    public Duration cleanupInterval() {
        return cleanupInterval;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (TransactionMonitoringConfigurationProperties) obj;
        return Objects.equals(this.maxTransactionsPerMethod, that.maxTransactionsPerMethod)
                && Objects.equals(this.cleanupInterval, that.cleanupInterval);
    }

    @Override
    public int hashCode() {
        return Objects.hash(maxTransactionsPerMethod, cleanupInterval);
    }

    @Override
    public String toString() {
        return "TransactionMonitoringConfigurationProperties[" + "maxTransactionsPerMethod="
                + maxTransactionsPerMethod + ", " + "cleanupInterval="
                + cleanupInterval + ']';
    }
}
