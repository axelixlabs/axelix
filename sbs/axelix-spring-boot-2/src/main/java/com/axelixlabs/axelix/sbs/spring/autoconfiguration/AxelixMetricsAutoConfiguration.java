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

import java.util.Set;

import io.micrometer.core.instrument.MeterRegistry;

import org.springframework.boot.actuate.autoconfigure.endpoint.condition.ConditionalOnAvailableEndpoint;
import org.springframework.boot.actuate.autoconfigure.metrics.CompositeMeterRegistryAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.metrics.MetricsAutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

import com.axelixlabs.axelix.common.api.transform.BaseUnitParser;
import com.axelixlabs.axelix.common.api.transform.BaseUnitValueTransformer;
import com.axelixlabs.axelix.common.api.transform.BytesMemoryBaseUnitValueTransformer;
import com.axelixlabs.axelix.common.api.transform.KilobytesMemoryBaseUnitValueTransformer;
import com.axelixlabs.axelix.sbs.spring.core.metrics.AxelixMetricsEndpoint;
import com.axelixlabs.axelix.sbs.spring.core.metrics.DefaultServiceMetricsGroupsAssembler;
import com.axelixlabs.axelix.sbs.spring.core.metrics.ServiceMetricsGroupsAssembler;

/**
 * Auto-configuration for the {@link AxelixMetricsEndpoint}.
 *
 * @since 17.11.2025
 * @author Nikita Kirillov
 * @author Mikhail Polivakha
 */
@AutoConfiguration(after = {MetricsAutoConfiguration.class, CompositeMeterRegistryAutoConfiguration.class})
@ConditionalOnAvailableEndpoint(endpoint = AxelixMetricsEndpoint.class)
@ConditionalOnBean(MeterRegistry.class)
public class AxelixMetricsAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public AxelixMetricsEndpoint axelixMetricsEndpoint(
            MeterRegistry registry,
            BaseUnitParser baseUnitParser,
            Set<BaseUnitValueTransformer> baseUnitValueTransformers,
            ServiceMetricsGroupsAssembler serviceMetricsGroupsAssembler) {
        return new AxelixMetricsEndpoint(
                registry, baseUnitParser, serviceMetricsGroupsAssembler, baseUnitValueTransformers);
    }

    @Bean
    @ConditionalOnMissingBean
    public BytesMemoryBaseUnitValueTransformer bytesMemoryBaseUnitValueTransformer() {
        return new BytesMemoryBaseUnitValueTransformer();
    }

    @Bean
    @ConditionalOnMissingBean
    public KilobytesMemoryBaseUnitValueTransformer kilobytesMemoryBaseUnitValueTransformer() {
        return new KilobytesMemoryBaseUnitValueTransformer();
    }

    @Bean
    @ConditionalOnMissingBean
    public BaseUnitParser baseUnitParser() {
        return new BaseUnitParser();
    }

    @Bean
    @ConditionalOnMissingBean
    public ServiceMetricsGroupsAssembler serviceMetricsGroupsAssembler(MeterRegistry registry) {
        return new DefaultServiceMetricsGroupsAssembler(registry);
    }
}
