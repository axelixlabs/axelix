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
package com.axelixlabs.axelix.sbs.spring.core.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.binder.MeterBinder;

import org.springframework.boot.actuate.metrics.MetricsEndpoint;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

import com.axelixlabs.axelix.common.api.transform.BaseUnitParser;
import com.axelixlabs.axelix.common.api.transform.BytesMemoryBaseUnitValueTransformer;
import com.axelixlabs.axelix.common.api.transform.KilobytesMemoryBaseUnitValueTransformer;
import com.axelixlabs.axelix.sbs.spring.core.Main;
import com.axelixlabs.axelix.sbs.spring.core.auth.JwtAuthTestConfiguration;

/**
 * Shared base test that defines a single Spring {@link ApplicationContext} reused by the
 * {@code metrics} integration tests so they hit the Spring TestContext cache instead of each
 * building their own context.
 *
 * <p>{@link Main} is pinned via the {@code classes} attribute (rather than left to Spring Boot's
 * auto-detection) so all subclasses resolve an identical, deterministic configuration — this is
 * what makes the cached context shareable. The metrics beans, the actuator {@link MetricsEndpoint}
 * and the {@link JwtAuthTestConfiguration} are imported here, and the shared metric fixtures live
 * in the nested {@link MetricsTestSupportConfiguration} — so the parent owns the entire shared
 * configuration and never has to reference its subclasses.
 *
 * @author Nikita Kirillov
 * @author Sergey Cherkasov
 * @author Artemiy Degtyarev
 */
@SpringBootTest(classes = Main.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import({
    AxelixMetricsEndpoint.class,
    MetricsEndpoint.class,
    DefaultServiceMetricsGroupsAssembler.class,
    BaseUnitParser.class,
    KilobytesMemoryBaseUnitValueTransformer.class,
    BytesMemoryBaseUnitValueTransformer.class,
    JwtAuthTestConfiguration.class,
    AbstractMetricsSharedContextTest.MetricsTestSupportConfiguration.class
})
abstract class AbstractMetricsSharedContextTest {

    @TestConfiguration
    static class MetricsTestSupportConfiguration {

        @Bean
        public MeterBinder groupingMetrics() {
            return registry -> {
                Counter.builder("axelixMetrics.test.metric1")
                        .description("Test metric belonging to the `axelixMetrics` group with a description")
                        .register(registry);

                Counter.builder("axelixMetrics.test.metric2")
                        .description("Test metric belonging to the `axelixMetrics` group with a description")
                        .register(registry);

                Counter.builder("axelixMetrics.test.metric3").register(registry);

                Counter.builder("testMetrics.axelix.metric1")
                        .description("Test metric belonging to the `testMetrics` group with a description")
                        .register(registry);

                Counter.builder("testMetrics.axelix.metric2").register(registry);

                Counter.builder("standalone")
                        .description(
                                "Test metric belonging to the 'Others' group without a prefix and with a description")
                        .register(registry);

                Gauge.builder(
                                "for.value.transformations", () -> 5480079 // ~ 5.22 MB
                                )
                        .baseUnit("bytes")
                        .register(registry);
            };
        }
    }
}
