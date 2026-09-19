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
package com.axelixlabs.axelix.sbs.spring.autoconfiguration;

import org.junit.jupiter.api.Test;

import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.health.actuate.endpoint.HealthEndpoint;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import com.axelixlabs.axelix.common.api.registration.BasicRegistrationMetadata;
import com.axelixlabs.axelix.sbs.spring.core.master.AxelixMetadataEndpoint;
import com.axelixlabs.axelix.sbs.spring.core.master.BasicRegistrationMetadataAssembler;
import com.axelixlabs.axelix.sbs.spring.core.persistence.transaction.DefaultTransactionStatsCollector;
import com.axelixlabs.axelix.sbs.spring.core.persistence.transaction.TransactionAttributesRegistry;
import com.axelixlabs.axelix.sbs.spring.core.persistence.transaction.TransactionStatsCollector;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link AxelixMetadataEndpointAutoConfiguration}.
 *
 * @author Mikhail Polivakha
 */
class AxelixMetadataEndpointAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withBean(TransactionStatsCollector.class, DefaultTransactionStatsCollector::new)
            .withBean(TransactionAttributesRegistry.class, TransactionAttributesRegistry::new)
            .withConfiguration(AutoConfigurations.of(
                    AxelixInfoPropertiesAutoConfiguration.class,
                    LibraryInformationProviderAutoConfiguration.class,
                    AxelixMetadataEndpointAutoConfiguration.class));

    @Test
    void shouldCreateMetadataEndpointWithoutHealthEndpoint() {
        contextRunner.run(context -> {
            assertThat(context).doesNotHaveBean(HealthEndpoint.class);
            assertThat(context).hasSingleBean(BasicRegistrationMetadataAssembler.class);
            assertThat(context).hasSingleBean(AxelixMetadataEndpoint.class);

            BasicRegistrationMetadata metadata =
                    context.getBean(BasicRegistrationMetadataAssembler.class).assemble();

            assertThat(metadata.getHealthStatus()).isEqualTo(BasicRegistrationMetadata.HealthStatus.UP);
        });
    }
}
