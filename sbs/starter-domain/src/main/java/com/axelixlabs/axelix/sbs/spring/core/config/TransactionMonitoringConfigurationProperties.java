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

import com.axelixlabs.axelix.common.utils.Assert;

/**
 * Configuration properties for transaction monitoring feature.
 *
 * @since 26.01.2026
 * @author Nikita Kirillov
 * @author Cherkasov Sergey
 * @author Mikhail Polivakha
 */
public class TransactionMonitoringConfigurationProperties implements Validateable {

    /**
     * Maximum number of transaction records to keep per method.
     */
    private Integer maxTransactionsPerMethod;

    /**
     * Interval for clearing old transaction records.
     */
    private Duration cleanupInterval;

    /**
     * Create a new TransactionMonitoringConfigurationProperties
     */
    public TransactionMonitoringConfigurationProperties() {
        this.maxTransactionsPerMethod = 30;
        this.cleanupInterval = Duration.ofSeconds(5);
    }

    public Integer getMaxTransactionsPerMethod() {
        return maxTransactionsPerMethod;
    }

    public Duration getCleanupInterval() {
        return cleanupInterval;
    }

    public TransactionMonitoringConfigurationProperties setMaxTransactionsPerMethod(Integer maxTransactionsPerMethod) {
        this.maxTransactionsPerMethod = maxTransactionsPerMethod;
        return this;
    }

    public TransactionMonitoringConfigurationProperties setCleanupInterval(Duration cleanupInterval) {
        this.cleanupInterval = cleanupInterval;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        TransactionMonitoringConfigurationProperties that = (TransactionMonitoringConfigurationProperties) o;
        return Objects.equals(maxTransactionsPerMethod, that.maxTransactionsPerMethod)
                && Objects.equals(cleanupInterval, that.cleanupInterval);
    }

    @Override
    public int hashCode() {
        return Objects.hash(maxTransactionsPerMethod, cleanupInterval);
    }

    @Override
    public String toString() {
        return "TransactionMonitoringConfigurationProperties{" + "maxTransactionsPerMethod="
                + maxTransactionsPerMethod + ", cleanupInterval="
                + cleanupInterval + '}';
    }

    @Override
    public void validate() throws IllegalArgumentException {
        Assert.isTrue(maxTransactionsPerMethod > 0, "maxTransactionsPerMethod must be positive");
        Assert.isTrue(cleanupInterval.toMillis() > 0, "cleanupInterval must be positive");
    }
}
