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

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;

import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import com.axelixlabs.axelix.common.api.transform.BaseUnitParser;
import com.axelixlabs.axelix.common.api.transform.BaseUnitValueTransformer;
import com.axelixlabs.axelix.common.api.transform.BytesMemoryBaseUnitValueTransformer;
import com.axelixlabs.axelix.common.api.transform.KilobytesMemoryBaseUnitValueTransformer;
import com.axelixlabs.axelix.sbs.spring.core.metrics.AxelixMetricsEndpoint;
import com.axelixlabs.axelix.sbs.spring.core.metrics.ServiceMetricsGroupsAssembler;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test for {@link AxelixMetricsAutoConfiguration}.
 *
 * @author Sergey Cherkasov
 */
class AxelixMetricsAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withPropertyValues("management.endpoints.web.exposure.include=axelix-metrics")
            .withBean(MeterRegistry.class, SimpleMeterRegistry::new)
            .withConfiguration(AutoConfigurations.of(AxelixMetricsAutoConfiguration.class));

    @Test
    void shouldCreateAllBeansInDefaultScenario() {
        contextRunner.run(context -> {
            assertThat(context).hasSingleBean(AxelixMetricsAutoConfiguration.class);
            assertThat(context).hasSingleBean(AxelixMetricsEndpoint.class);
            assertThat(context).hasSingleBean(BytesMemoryBaseUnitValueTransformer.class);
            assertThat(context).hasSingleBean(KilobytesMemoryBaseUnitValueTransformer.class);
            assertThat(context).hasSingleBean(BaseUnitParser.class);
            assertThat(context).hasSingleBean(ServiceMetricsGroupsAssembler.class);
            assertThat(context).getBeans(BaseUnitValueTransformer.class).hasSize(2);
        });
    }

    @Test
    void shouldNotActivateAutoConfigurationWithoutRequiredProperty() {
        new ApplicationContextRunner()
                .withBean(MeterRegistry.class, SimpleMeterRegistry::new)
                .withConfiguration(AutoConfigurations.of(AxelixMetricsAutoConfiguration.class))
                .run(context -> {
                    assertThat(context).doesNotHaveBean(AxelixMetricsAutoConfiguration.class);
                    assertThat(context).doesNotHaveBean(AxelixMetricsEndpoint.class);
                    assertThat(context).doesNotHaveBean(BytesMemoryBaseUnitValueTransformer.class);
                    assertThat(context).doesNotHaveBean(KilobytesMemoryBaseUnitValueTransformer.class);
                    assertThat(context).doesNotHaveBean(BaseUnitParser.class);
                    assertThat(context).doesNotHaveBean(ServiceMetricsGroupsAssembler.class);
                });
    }

    @Test
    void shouldNotActivateAutoConfigurationWithoutMeterRegistry() {
        new ApplicationContextRunner()
                .withPropertyValues("management.endpoints.web.exposure.include=axelix-metrics")
                .withConfiguration(AutoConfigurations.of(AxelixMetricsAutoConfiguration.class))
                .run(context -> {
                    assertThat(context).doesNotHaveBean(AxelixMetricsAutoConfiguration.class);
                    assertThat(context).doesNotHaveBean(AxelixMetricsEndpoint.class);
                    assertThat(context).doesNotHaveBean(BytesMemoryBaseUnitValueTransformer.class);
                    assertThat(context).doesNotHaveBean(KilobytesMemoryBaseUnitValueTransformer.class);
                    assertThat(context).doesNotHaveBean(BaseUnitParser.class);
                    assertThat(context).doesNotHaveBean(ServiceMetricsGroupsAssembler.class);
                });
    }
}
