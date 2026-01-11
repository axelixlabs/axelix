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
package com.nucleonforge.axelix.sbs.autoconfiguration.spring;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

import com.nucleonforge.axelix.sbs.spring.configprops.AxelixConfigurationPropertiesEndpoint;
import com.nucleonforge.axelix.sbs.spring.configprops.ConfigPropsConfigurationProperties;
import com.nucleonforge.axelix.sbs.spring.configprops.ConfigurationPropertiesCache;
import com.nucleonforge.axelix.sbs.spring.configprops.ConfigurationPropertiesConverter;
import com.nucleonforge.axelix.sbs.spring.configprops.FlatteningConfigurationPropertiesConverter;
import com.nucleonforge.axelix.sbs.spring.configprops.SmartSanitizingFunction;
import com.nucleonforge.axelix.sbs.spring.env.DefaultPropertyNameNormalizer;
import com.nucleonforge.axelix.sbs.spring.env.PropertyNameNormalizer;

/**
 * Auto-configuration for the {@link AxelixConfigurationPropertiesEndpoint}.
 *
 * @since 13.11.2025
 * @author Sergey Cherkasov
 */
@AutoConfiguration
@EnableConfigurationProperties(ConfigPropsConfigurationProperties.class)
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
            ConfigPropsConfigurationProperties configPropsConfigurationProperties,
            PropertyNameNormalizer propertyNameNormalizer) {
        return new SmartSanitizingFunction(
                configPropsConfigurationProperties.getSanitizedProperties(), propertyNameNormalizer);
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
