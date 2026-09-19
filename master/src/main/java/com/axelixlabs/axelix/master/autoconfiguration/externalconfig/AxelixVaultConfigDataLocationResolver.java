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

import java.util.Collections;
import java.util.List;

import org.springframework.boot.context.config.ConfigDataLocation;
import org.springframework.boot.context.config.ConfigDataLocationNotFoundException;
import org.springframework.boot.context.config.ConfigDataLocationResolver;
import org.springframework.boot.context.config.ConfigDataLocationResolverContext;
import org.springframework.boot.context.config.Profiles;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.cloud.vault.config.VaultConfigDataLocationResolver;
import org.springframework.cloud.vault.config.VaultConfigLocation;
import org.springframework.cloud.vault.config.VaultProperties;
import org.springframework.core.Ordered;

/**
 * Custom {@link ConfigDataLocationResolver} for Vault-based configuration data locations.
 *
 * <p>This resolver intercepts Vault config location resolution to bind and register Axelix-specific
 * {@link AxelixVaultProperties}.
 *
 * @author Ilya Naumov
 */
public class AxelixVaultConfigDataLocationResolver extends VaultConfigDataLocationResolver implements Ordered {
    /**
     * Returns {@link Ordered#HIGHEST_PRECEDENCE} ensuring this resolver takes precedence
     * over {@link VaultConfigDataLocationResolver}.
     */
    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }

    /**
     * Binds Axelix-specific Vault properties from the prefix
     * {@value AxelixVaultProperties#AXELIX_PREFIX}, sets the application name
     * from {@code spring.application.name}, and registers the resolved
     * {@link AxelixVaultProperties} in the bootstrap context for downstream use.
     */
    @Override
    public List<VaultConfigLocation> resolveProfileSpecific(
            ConfigDataLocationResolverContext context, ConfigDataLocation location, Profiles profiles)
            throws ConfigDataLocationNotFoundException {

        if (!location.getValue().startsWith(VaultConfigLocation.VAULT_PREFIX)) {
            return Collections.emptyList();
        }

        AxelixVaultProperties vaultProperties = context.getBinder()
                .bind(AxelixVaultProperties.AXELIX_PREFIX, Bindable.of(AxelixVaultProperties.class))
                .orElse(null);

        if (vaultProperties != null) {
            vaultProperties.setApplicationName(context.getBinder()
                    .bind("spring.application.name", String.class)
                    .orElse(vaultProperties.getApplicationName()));
            context.getBootstrapContext().registerIfAbsent(VaultProperties.class, ignore -> vaultProperties);
        }

        return super.resolveProfileSpecific(context, location, profiles);
    }
}
