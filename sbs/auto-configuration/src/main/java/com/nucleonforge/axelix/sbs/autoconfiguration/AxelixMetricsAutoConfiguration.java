/*
 * Copyright 2025-present, Nucleon Forge Software.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.nucleonforge.axelix.sbs.autoconfiguration;

import java.util.Set;

import io.micrometer.core.instrument.MeterRegistry;

import org.springframework.boot.actuate.autoconfigure.endpoint.condition.ConditionalOnAvailableEndpoint;
import org.springframework.boot.actuate.autoconfigure.metrics.MetricsAutoConfiguration;
import org.springframework.boot.actuate.metrics.MetricsEndpoint;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

import com.nucleonforge.axelix.common.api.transform.BaseUnitParser;
import com.nucleonforge.axelix.common.api.transform.BaseUnitValueTransformer;
import com.nucleonforge.axelix.common.api.transform.BytesMemoryBaseUnitValueTransformer;
import com.nucleonforge.axelix.common.api.transform.KilobytesMemoryBaseUnitValueTransformer;
import com.nucleonforge.axelix.sbs.spring.metrics.AxelixMetricsEndpoint;
import com.nucleonforge.axelix.sbs.spring.metrics.DefaultServiceMetricsGroupsAssembler;
import com.nucleonforge.axelix.sbs.spring.metrics.ServiceMetricsGroupsAssembler;

/**
 * Auto-configuration for the {@link AxelixMetricsEndpoint}.
 *
 * @since 17.11.2025
 * @author Nikita Kirillov
 * @author Mikhail Polivakha
 */
@AutoConfiguration(after = MetricsAutoConfiguration.class)
@ConditionalOnAvailableEndpoint(endpoint = MetricsEndpoint.class)
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
    public ServiceMetricsGroupsAssembler defaultMetricsGroupsAssembler(MeterRegistry registry) {
        return new DefaultServiceMetricsGroupsAssembler(registry);
    }
}
