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

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import org.springframework.boot.bootstrap.BootstrapRegistry;
import org.springframework.boot.bootstrap.ConfigurableBootstrapContext;
import org.springframework.boot.context.config.ConfigDataLocation;
import org.springframework.boot.context.config.ConfigDataLocationResolverContext;
import org.springframework.boot.context.config.Profiles;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.cloud.vault.config.VaultConfigLocation;
import org.springframework.cloud.vault.config.VaultProperties;
import org.springframework.core.Ordered;
import org.springframework.core.env.Environment;
import org.springframework.mock.env.MockEnvironment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link AxelixVaultConfigDataLocationResolver}.
 *
 * @author Ilya Naumov
 */
class AxelixVaultConfigDataLocationResolverTest {
    private final AxelixVaultConfigDataLocationResolver resolver = new AxelixVaultConfigDataLocationResolver();

    @Test // GH-1489
    void shouldReturnHighestPrecedence() {
        // when.
        assertThat(resolver.getOrder())
                // then.
                .isEqualTo(Ordered.HIGHEST_PRECEDENCE);
    }

    @Nested
    class ResolveProfileSpecific {
        private ConfigDataLocationResolverContext resolverContext;
        private ConfigurableBootstrapContext bootstrapContext;
        private ConfigDataLocation location;
        private Profiles profiles;

        @BeforeEach
        void setUp() {
            resolverContext = mock(ConfigDataLocationResolverContext.class);
            location = mock(ConfigDataLocation.class);
            bootstrapContext = mock(ConfigurableBootstrapContext.class);
            profiles = mock(Profiles.class);

            when(resolverContext.getBootstrapContext()).thenReturn(bootstrapContext);
            when(location.getValue()).thenReturn("vault://");
        }

        void binderWith(Environment environment) {
            Binder binder = Binder.get(environment);
            when(resolverContext.getBinder()).thenReturn(binder);
        }

        @Test // GH-1489
        void shouldBindAndRegisterAxelixVaultProperties() {
            // given.
            binderWith(new MockEnvironment()
                    .withProperty("spring.application.name", "axelix")
                    .withProperty("spring.cloud.vault.enabled", true)
                    .withProperty("spring.cloud.vault.uri", "http://vault:8200")
                    .withProperty(AxelixVaultProperties.AXELIX_PREFIX + ".enabled", false)
                    .withProperty(AxelixVaultProperties.AXELIX_PREFIX + ".uri", "http://axelix-vault:8200"));

            // when.
            resolver.resolveProfileSpecific(resolverContext, location, profiles);

            // then.
            ArgumentCaptor<BootstrapRegistry.InstanceSupplier<VaultProperties>> captor = ArgumentCaptor.captor();
            verify(bootstrapContext, times(2)).registerIfAbsent(eq(VaultProperties.class), captor.capture());
            VaultProperties vaultProperty = captor.getAllValues().getFirst().get(bootstrapContext);
            assertThat(vaultProperty).isNotNull();
            assertThat(vaultProperty.getApplicationName()).isEqualTo("axelix");
            assertThat(vaultProperty.isEnabled()).isFalse();
            assertThat(vaultProperty.getUri()).isEqualTo("http://axelix-vault:8200");
        }

        @Test // GH-1489
        void shouldUseDefaultVaultPropertiesWhenNoAxelixConfig() {
            // given.
            binderWith(new MockEnvironment()
                    .withProperty("spring.cloud.vault.enabled", false)
                    .withProperty("spring.cloud.vault.uri", "http://vault:8200"));

            // when.
            resolver.resolveProfileSpecific(resolverContext, location, profiles);

            // then.
            ArgumentCaptor<BootstrapRegistry.InstanceSupplier<VaultProperties>> captor = ArgumentCaptor.captor();
            verify(bootstrapContext, times(1)).registerIfAbsent(eq(VaultProperties.class), captor.capture());
            VaultProperties vaultProperty = captor.getValue().get(bootstrapContext);
            assertThat(vaultProperty).isNotNull();
            assertThat(vaultProperty.isEnabled()).isFalse();
            assertThat(vaultProperty.getUri()).isEqualTo("http://vault:8200");
        }

        @Test // GH-1489
        void shouldReturnEmptyListWhenNonVaultLocation() {
            // given.
            when(location.getValue()).thenReturn("configserver:");

            // when.
            List<VaultConfigLocation> result = resolver.resolveProfileSpecific(resolverContext, location, profiles);

            // then.
            assertThat(result).isEmpty();
        }
    }
}
