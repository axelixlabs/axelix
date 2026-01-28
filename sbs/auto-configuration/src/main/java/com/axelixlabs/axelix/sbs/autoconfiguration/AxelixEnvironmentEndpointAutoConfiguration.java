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
package com.axelixlabs.axelix.sbs.autoconfiguration;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;

import com.axelixlabs.axelix.sbs.spring.config.EndpointsConfigurationProperties;
import com.axelixlabs.axelix.sbs.spring.configprops.ConfigurationPropertiesCache;
import com.axelixlabs.axelix.sbs.spring.configprops.SmartSanitizingFunction;
import com.axelixlabs.axelix.sbs.spring.env.AxelixEnvironmentEndpoint;
import com.axelixlabs.axelix.sbs.spring.env.DefaultEnvPropertyEnricher;
import com.axelixlabs.axelix.sbs.spring.env.DefaultPropertyMetadataExtractor;
import com.axelixlabs.axelix.sbs.spring.env.DefaultPropertyNameNormalizer;
import com.axelixlabs.axelix.sbs.spring.env.EnvPropertyEnricher;
import com.axelixlabs.axelix.sbs.spring.env.PropertyMetadataExtractor;
import com.axelixlabs.axelix.sbs.spring.env.PropertyNameNormalizer;
import com.axelixlabs.axelix.sbs.spring.env.ValueInjectionTrackerBeanPostProcessor;

/**
 * Auto-configuration for the {@link AxelixEnvironmentEndpoint}.
 *
 * @since 21.10.2025
 * @author Nikita Kirillov
 * @author Mikhail Polivakha
 */
@AutoConfiguration(after = {AxelixConfigurationsPropertiesEndpointAutoConfiguration.class})
public class AxelixEnvironmentEndpointAutoConfiguration {

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
    public SmartSanitizingFunction smartSanitizingFunction(
            EndpointsConfigurationProperties endpointsConfigurationProperties,
            PropertyNameNormalizer propertyNameNormalizer) {
        return new SmartSanitizingFunction(
                endpointsConfigurationProperties.getSanitizedProperties(), propertyNameNormalizer);
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
    public AxelixEnvironmentEndpoint axelixEnvironmentEndpoint(
            Environment environment,
            SmartSanitizingFunction smartSanitizingFunction,
            EnvPropertyEnricher envPropertyEnricher) {
        return new AxelixEnvironmentEndpoint(environment, smartSanitizingFunction, envPropertyEnricher);
    }

    @Bean
    @ConditionalOnMissingBean
    public ValueInjectionTrackerBeanPostProcessor valueInjectionTrackerBeanPostProcessor(
            PropertyNameNormalizer propertyNameNormalizer) {
        return new ValueInjectionTrackerBeanPostProcessor(propertyNameNormalizer);
    }
}
