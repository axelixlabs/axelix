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
package com.nucleonforge.axelix.sbs.spring.env;

import java.util.List;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;

import com.nucleonforge.axelix.sbs.spring.configprops.ConfigurationPropertiesCache;
import com.nucleonforge.axelix.sbs.spring.configprops.ConfigurationPropertiesConverter;
import com.nucleonforge.axelix.sbs.spring.configprops.FlatteningConfigurationPropertiesConverter;
import com.nucleonforge.axelix.sbs.spring.configprops.SmartSanitizingFunction;

/**
 * Environment test configuration.
 *
 * @author Mikhail Polivakha
 * @author Nikita Kirillov
 */
@TestConfiguration
public class EnvironmentTestConfig {

    @Bean
    public ConfigurationPropertiesConverter configurationPropertiesConverter() {
        return new FlatteningConfigurationPropertiesConverter();
    }

    @Bean
    public SmartSanitizingFunction smartSanitizingFunction(PropertyNameNormalizer propertyNameNormalizer) {
        return new SmartSanitizingFunction(List.of(), propertyNameNormalizer);
    }

    @Bean
    public ConfigurationPropertiesCache configurationPropertiesCache(
            SmartSanitizingFunction smartSanitizingFunction,
            ApplicationContext applicationContext,
            ConfigurationPropertiesConverter configurationPropertiesConverter) {
        return new ConfigurationPropertiesCache(
                smartSanitizingFunction, applicationContext, configurationPropertiesConverter);
    }

    @Bean
    public PropertyNameNormalizer propertyNameNormalizer() {
        return new DefaultPropertyNameNormalizer();
    }

    @Bean
    public PropertyMetadataExtractor propertyMetadataExtractor(
            ConfigurableEnvironment environment, PropertyNameNormalizer propertyNameNormalizer) {
        return new DefaultPropertyMetadataExtractor(environment, propertyNameNormalizer);
    }

    @Bean
    public ValueInjectionTrackerBeanPostProcessor trackingAutowiredAnnotationBeanPostProcessor(
            PropertyNameNormalizer propertyNameNormalizer) {
        return new ValueInjectionTrackerBeanPostProcessor(propertyNameNormalizer);
    }

    @Bean
    public EnvPropertyEnricher envPropertyEnricher(
            Environment environment,
            DefaultPropertyNameNormalizer propertyNameNormalizer,
            ObjectProvider<ConfigurationPropertiesCache> configurationPropertiesCache,
            PropertyMetadataExtractor propertyMetadataExtractor,
            ValueInjectionTrackerBeanPostProcessor valueInjectionTrackerBeanPostProcessor) {
        return new DefaultEnvPropertyEnricher(
                environment,
                propertyNameNormalizer,
                configurationPropertiesCache,
                propertyMetadataExtractor,
                valueInjectionTrackerBeanPostProcessor);
    }
}
