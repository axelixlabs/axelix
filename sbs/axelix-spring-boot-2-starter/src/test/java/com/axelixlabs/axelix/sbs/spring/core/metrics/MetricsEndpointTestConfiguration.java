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
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

import com.axelixlabs.axelix.common.api.transform.BaseUnitParser;
import com.axelixlabs.axelix.common.api.transform.BytesMemoryBaseUnitValueTransformer;
import com.axelixlabs.axelix.common.api.transform.KilobytesMemoryBaseUnitValueTransformer;

/**
 * Test configuration for {@link AxelixMetricsEndpointTest}, part of the shared endpoint test context.
 *
 * @author Nikita Kirillov
 * @author Sergey Cherkasov
 */
@TestConfiguration
@Import({
    AxelixMetricsEndpoint.class,
    MetricsEndpoint.class,
    DefaultServiceMetricsGroupsAssembler.class,
    BaseUnitParser.class,
    KilobytesMemoryBaseUnitValueTransformer.class,
    BytesMemoryBaseUnitValueTransformer.class
})
public class MetricsEndpointTestConfiguration {

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
                    .description("Test metric belonging to the 'Others' group without a prefix and with a description")
                    .register(registry);

            Gauge.builder(
                            "for.value.transformations", () -> 5480079 // ~ 5.22 MB
                            )
                    .baseUnit("bytes")
                    .register(registry);
        };
    }
}
