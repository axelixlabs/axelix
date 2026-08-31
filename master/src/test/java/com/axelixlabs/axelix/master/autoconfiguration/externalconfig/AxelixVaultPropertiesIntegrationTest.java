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

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.bootstrap.ConfigurableBootstrapContext;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.vault.config.VaultProperties;
import org.springframework.context.ApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link AxelixVaultConfigDataLocationResolver}.
 * <p>
 * Ensures that the resolver is registered via {@code spring.factories}
 * and is actually invoked at runtime during config data loading.
 *
 * @author Ilya Naumov
 */
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.NONE,
        properties = {
            "spring.config.import=optional:vault://",
            "spring.application.name=axelix",
            "spring.cloud.vault.uri=http://vault:8200",
            "spring.cloud.vault.enabled=true",
            AxelixVaultProperties.AXELIX_PREFIX + ".enabled=false",
            AxelixVaultProperties.AXELIX_PREFIX + ".uri=http://axelix-vault:8200"
        })
class AxelixVaultPropertiesIntegrationTest {
    @Autowired
    private ApplicationContext applicationContext;

    @Test // GH-1489
    void shouldRegisterAxelixVaultPropertiesInBootstrapContext() {
        ConfigurableBootstrapContext bootstrapContext = applicationContext.getBean(ConfigurableBootstrapContext.class);

        VaultProperties props = bootstrapContext.get(VaultProperties.class);

        assertThat(props).isNotNull();
        assertThat(props.getApplicationName()).isEqualTo("axelix");
        assertThat(props.isEnabled()).isFalse();
        assertThat(props.getUri()).isEqualTo("http://axelix-vault:8200");
    }
}
