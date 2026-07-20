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

import java.util.Objects;

import com.axelixlabs.axelix.common.utils.Assert;

/**
 * Configuration properties for transaction monitoring feature.
 *
 * @since 26.01.2026
 * @author Nikita Kirillov
 * @author Cherkasov Sergey
 * @author Mikhail Polivakha
 * @author Ilya Naumov
 */
public class TransactionMonitoringConfigurationProperties implements Validatable {

    public static final String CONFIG_PROPS_PREFIX = "axelix.sbs.transaction.monitoring";

    /**
     * Whether transaction monitoring is enabled. When {@code false}, the transaction monitoring
     * auto-configuration backs off entirely.
     */
    private Boolean enabled;

    /**
     * Maximum number of transaction records to keep per method.
     */
    private Integer maxTransactionsPerMethod;

    /**
     * Create a new TransactionMonitoringConfigurationProperties
     */
    public TransactionMonitoringConfigurationProperties() {
        this.enabled = true;
        this.maxTransactionsPerMethod = 30;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public TransactionMonitoringConfigurationProperties setEnabled(Boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    public Integer getMaxTransactionsPerMethod() {
        return maxTransactionsPerMethod;
    }

    public TransactionMonitoringConfigurationProperties setMaxTransactionsPerMethod(Integer maxTransactionsPerMethod) {
        this.maxTransactionsPerMethod = maxTransactionsPerMethod;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        TransactionMonitoringConfigurationProperties that = (TransactionMonitoringConfigurationProperties) o;
        return Objects.equals(enabled, that.enabled)
                && Objects.equals(maxTransactionsPerMethod, that.maxTransactionsPerMethod);
    }

    @Override
    public int hashCode() {
        return Objects.hash(enabled, maxTransactionsPerMethod);
    }

    @Override
    public String toString() {
        return "TransactionMonitoringConfigurationProperties{" + "enabled=" + enabled + ", maxTransactionsPerMethod="
                + maxTransactionsPerMethod + '}';
    }

    @Override
    public void validate() throws IllegalArgumentException {
        Assert.isTrue(maxTransactionsPerMethod > 0, "maxTransactionsPerMethod must be positive");
    }

    public static class InMemoryPaginationDetection {
        /**
         * Whether in-memory pagination detection is enabled.
         */
        private Boolean enabled;

        public InMemoryPaginationDetection(Boolean enabled) {
            this.enabled = enabled;
        }

        public Boolean getEnabled() {
            return enabled;
        }

        public void setEnabled(Boolean enabled) {
            this.enabled = enabled;
        }

        @Override
        public boolean equals(Object o) {
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            InMemoryPaginationDetection that = (InMemoryPaginationDetection) o;
            return Objects.equals(enabled, that.enabled);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(enabled);
        }

        @Override
        public String toString() {
            return "InMemoryPaginationDetection{" + "enabled=" + enabled + '}';
        }
    }
}
