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
package com.nucleonforge.axile.sbs.autoconfiguration.spring;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.actuate.autoconfigure.endpoint.condition.ConditionalOnAvailableEndpoint;
import org.springframework.boot.actuate.autoconfigure.env.EnvironmentEndpointAutoConfiguration;
import org.springframework.boot.actuate.env.EnvironmentEndpoint;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;

import com.nucleonforge.axile.sbs.spring.configprops.ConfigurationPropertiesCache;
import com.nucleonforge.axile.sbs.spring.env.AxileEnvironmentEndpoint;
import com.nucleonforge.axile.sbs.spring.env.DefaultEnvPropertyEnricher;
import com.nucleonforge.axile.sbs.spring.env.DefaultPropertyMetadataExtractor;
import com.nucleonforge.axile.sbs.spring.env.DefaultPropertyNameNormalizer;
import com.nucleonforge.axile.sbs.spring.env.EnvPropertyEnricher;
import com.nucleonforge.axile.sbs.spring.env.PropertyMetadataExtractor;
import com.nucleonforge.axile.sbs.spring.env.PropertyNameNormalizer;
import com.nucleonforge.axile.sbs.spring.env.ValueInjectionTrackerBeanPostProcessor;

/**
 * Auto-configuration for the {@link AxileEnvironmentEndpoint}.
 *
 * @since 21.10.2025
 * @author Nikita Kirillov
 */
@AutoConfiguration(
        after = {
            EnvironmentEndpointAutoConfiguration.class,
            AxileConfigurationsPropertiesEndpointAutoConfiguration.class
        })
@ConditionalOnAvailableEndpoint(endpoint = EnvironmentEndpoint.class)
public class AxileEnvironmentEndpointAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public PropertyNameNormalizer propertyNameNormalizer() {
        return new DefaultPropertyNameNormalizer();
    }

    @Bean
    @ConditionalOnMissingBean
    public PropertyMetadataExtractor propertyMetadataExtractor(
            ConfigurableEnvironment configurableEnvironment, PropertyNameNormalizer propertyNameNormalizer) {
        return new DefaultPropertyMetadataExtractor(configurableEnvironment, propertyNameNormalizer);
    }

    @Bean
    @ConditionalOnMissingBean
    public EnvPropertyEnricher envPropertyEnricher(
            Environment environment,
            PropertyNameNormalizer propertyNameNormalizer,
            ObjectProvider<ConfigurationPropertiesCache> cache,
            PropertyMetadataExtractor propertyMetadataExtractor,
            ValueInjectionTrackerBeanPostProcessor injectionTracker) {
        return new DefaultEnvPropertyEnricher(
                environment, propertyNameNormalizer, cache, propertyMetadataExtractor, injectionTracker);
    }

    @Bean
    @ConditionalOnMissingBean
    public AxileEnvironmentEndpoint axileEnvironmentEndpoint(
            EnvironmentEndpoint environmentEndpoint, EnvPropertyEnricher envPropertyEnricher) {
        return new AxileEnvironmentEndpoint(environmentEndpoint, envPropertyEnricher);
    }

    @Bean
    @ConditionalOnMissingBean
    public ValueInjectionTrackerBeanPostProcessor trackingAutowiredAnnotationBeanPostProcessor(
            PropertyNameNormalizer propertyNameNormalizer) {
        return new ValueInjectionTrackerBeanPostProcessor(propertyNameNormalizer);
    }
}
