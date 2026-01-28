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

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

import com.axelixlabs.axelix.sbs.spring.config.EndpointsConfigurationProperties;
import com.axelixlabs.axelix.sbs.spring.configprops.AxelixConfigurationPropertiesEndpoint;
import com.axelixlabs.axelix.sbs.spring.configprops.ConfigurationPropertiesCache;
import com.axelixlabs.axelix.sbs.spring.configprops.ConfigurationPropertiesConverter;
import com.axelixlabs.axelix.sbs.spring.configprops.FlatteningConfigurationPropertiesConverter;
import com.axelixlabs.axelix.sbs.spring.configprops.SmartSanitizingFunction;
import com.axelixlabs.axelix.sbs.spring.env.DefaultPropertyNameNormalizer;
import com.axelixlabs.axelix.sbs.spring.env.PropertyNameNormalizer;

/**
 * Auto-configuration for the {@link AxelixConfigurationPropertiesEndpoint}.
 *
 * @since 13.11.2025
 * @author Sergey Cherkasov
 */
@AutoConfiguration
@EnableConfigurationProperties(EndpointsConfigurationProperties.class)
public class AxelixConfigurationsPropertiesEndpointAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public ConfigurationPropertiesConverter configurationPropertiesConverter() {
        return new FlatteningConfigurationPropertiesConverter();
    }

    @Bean
    @ConditionalOnMissingBean
    public PropertyNameNormalizer propertyNameNormalizer() {
        return new DefaultPropertyNameNormalizer();
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
    public ConfigurationPropertiesCache configurationPropertiesCaches(
            SmartSanitizingFunction smartSanitizingFunction,
            ApplicationContext applicationContext,
            ConfigurationPropertiesConverter configurationPropertiesConverter) {
        return new ConfigurationPropertiesCache(
                smartSanitizingFunction, applicationContext, configurationPropertiesConverter);
    }

    @Bean
    @ConditionalOnMissingBean
    public AxelixConfigurationPropertiesEndpoint axelixConfigurationPropertiesEndpoint(
            ConfigurationPropertiesCache configurationPropertiesCache) {
        return new AxelixConfigurationPropertiesEndpoint(configurationPropertiesCache);
    }
}
