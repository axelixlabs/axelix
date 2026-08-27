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
package com.axelixlabs.axelix.master.autoconfiguration.externalconfig;

import java.util.Map;

import org.springframework.core.Ordered;

/**
 * Supported external configuration sources. Each option defines the import location
 * for {@code spring.config.import} and the properties that must be enabled for the
 * underlying source.
 *
 * <p>The {@link Ordered#getOrder()} value determines the import sequence:
 * options with lower order values are imported first.
 *
 * @author Ilya Naumov
 */
public enum ExternalConfigOption implements Ordered {
    /**
     * HashiCorp Vault.
     *
     * <p>Has the lowest precedence ({@link Ordered#LOWEST_PRECEDENCE}), so it is
     * imported last in {@code spring.config.import}. Values loaded from Vault have
     * the highest priority and override properties from all previously sources.
     */
    VAULT(Ordered.LOWEST_PRECEDENCE, "optional:vault://", Map.of("spring.cloud.vault.enabled", true));

    private final int importOrder;
    private final String importLocation;
    private final Map<String, Object> properties;

    ExternalConfigOption(int importOrder, String importLocation, Map<String, Object> properties) {
        this.importOrder = importOrder;
        this.importLocation = importLocation;
        this.properties = properties;
    }

    @Override
    public int getOrder() {
        return importOrder;
    }

    public String getImportLocation() {
        return importLocation;
    }

    public Map<String, Object> getProperties() {
        return properties;
    }
}
