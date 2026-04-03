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

import java.util.Collection;
import java.util.function.Supplier;

import org.junit.jupiter.api.Test;

import org.springframework.boot.actuate.context.properties.ConfigurationPropertiesReportEndpoint;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.ConfigurableEnvironment;

import com.axelixlabs.axelix.common.api.ConfigurationPropertiesFeed;
import com.axelixlabs.axelix.sbs.spring.core.config.EndpointsConfigurationProperties;
import com.axelixlabs.axelix.sbs.spring.core.configprops.AxelixConfigurationPropertiesEndpoint;
import com.axelixlabs.axelix.sbs.spring.core.configprops.ConfigurationPropertiesBeansCache;
import com.axelixlabs.axelix.sbs.spring.core.configprops.ConfigurationPropertiesCache;
import com.axelixlabs.axelix.sbs.spring.core.configprops.ConfigurationPropertiesConverter;
import com.axelixlabs.axelix.sbs.spring.core.configprops.ConfigurationPropertiesMutabilityChecker;
import com.axelixlabs.axelix.sbs.spring.core.configprops.ConfigurationPropertiesMutator;
import com.axelixlabs.axelix.sbs.spring.core.configprops.ConfigurationPropertiesRuntimeValidator;
import com.axelixlabs.axelix.sbs.spring.core.configprops.RebindingConfigurationPropertiesMutator;
import com.axelixlabs.axelix.sbs.spring.core.env.PropertyNameNormalizer;
import com.axelixlabs.axelix.sbs.spring.core.properties.PropertyNameDiscoverer;
import com.axelixlabs.axelix.sbs.spring.core.properties.SmartSanitizingFunction;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test for {@link AxelixConfigurationsPropertiesEndpointAutoConfiguration}
 *
 * @since 09.02.2026
 * @author Nikita Kirillov
 */
class AxelixConfigurationsPropertiesEndpointAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(
                    AxelixConfigurationsPropertiesEndpointAutoConfiguration.class,
                    EndpointsConfigurationPropertiesAutoConfiguration.class))
            .withPropertyValues("management.endpoints.web.exposure.include=axelix-configprops");

    @Test
    void shouldCreateAllBeansInDefaultScenario() {
        contextRunner.run(context -> {
            assertThat(context).hasSingleBean(ConfigurationPropertiesConverter.class);
            assertThat(context).hasSingleBean(PropertyNameNormalizer.class);
            assertThat(context).hasSingleBean(SmartSanitizingFunction.class);
            assertThat(context).hasSingleBean(ConfigurationPropertiesCache.class);
            assertThat(context).hasSingleBean(AxelixConfigurationPropertiesEndpoint.class);
        });
    }

    @Test
    void shouldNotActivateAutoConfigurationWhenEndpointDisabled() {
        contextRunner // Overriding the property value to test the disabled state
                .withPropertyValues("management.endpoints.web.exposure.exclude=axelix-configprops")
                .run(context -> {
                    assertThat(context).doesNotHaveBean(AxelixConfigurationsPropertiesEndpointAutoConfiguration.class);
                    assertThat(context).doesNotHaveBean(AxelixConfigurationPropertiesEndpoint.class);
                    assertThat(context).doesNotHaveBean(ConfigurationPropertiesCache.class);
                    assertThat(context).doesNotHaveBean(SmartSanitizingFunction.class);
                    assertThat(context).doesNotHaveBean(PropertyNameNormalizer.class);
                    assertThat(context).doesNotHaveBean(ConfigurationPropertiesConverter.class);
                });
    }

    @Test
    void shouldNotActivateAutoConfigurationWithoutRequiredProperty() {
        ApplicationContextRunner runnerWithoutRequiredConfig = new ApplicationContextRunner()
                .withConfiguration(
                        AutoConfigurations.of(AxelixConfigurationsPropertiesEndpointAutoConfiguration.class));

        runnerWithoutRequiredConfig.run(context -> {
            assertThat(context).doesNotHaveBean(AxelixConfigurationsPropertiesEndpointAutoConfiguration.class);
            assertThat(context).doesNotHaveBean(AxelixConfigurationPropertiesEndpoint.class);
            assertThat(context).doesNotHaveBean(ConfigurationPropertiesCache.class);
            assertThat(context).doesNotHaveBean(SmartSanitizingFunction.class);
            assertThat(context).doesNotHaveBean(PropertyNameNormalizer.class);
            assertThat(context).doesNotHaveBean(ConfigurationPropertiesConverter.class);
        });
    }

    @Test
    void shouldHandleMultipleCustomBeans() {
        contextRunner
                .withUserConfiguration(
                        CustomConfigurationPropertiesConverterConfig.class,
                        CustomPropertyNameNormalizerConfig.class,
                        CustomSmartSanitizingFunctionConfig.class,
                        CustomConfigurationPropertiesCacheConfig.class,
                        CustomConfigurationPropertiesMutatorConfig.class,
                        CustomConfigurationPropertiesMutabilityCheckerConfig.class,
                        CustomConfigurationPropertiesRuntimeValidator.class,
                        CustomAxelixConfigurationPropertiesEndpointConfig.class)
                .run(context -> {
                    assertThat(context.getBean(ConfigurationPropertiesConverter.class))
                            .isExactlyInstanceOf(CustomConfigurationPropertiesConverter.class);
                    assertThat(context.getBean(PropertyNameNormalizer.class))
                            .isExactlyInstanceOf(CustomPropertyNameNormalizer.class);
                    assertThat(context.getBean(SmartSanitizingFunction.class))
                            .isExactlyInstanceOf(CustomSmartSanitizingFunction.class);
                    assertThat(context.getBean(ConfigurationPropertiesCache.class))
                            .isExactlyInstanceOf(CustomConfigurationPropertiesCache.class);
                    assertThat(context.getBean(AxelixConfigurationPropertiesEndpoint.class))
                            .isExactlyInstanceOf(CustomAxelixConfigurationPropertiesEndpoint.class);
                    assertThat(context.getBean(CustomConfigurationPropertiesMutator.class))
                            .isExactlyInstanceOf(CustomConfigurationPropertiesMutator.class);
                    assertThat(context.getBean(CustomConfigurationPropertiesRuntimeValidator.class))
                            .isExactlyInstanceOf(CustomConfigurationPropertiesRuntimeValidator.class);
                    assertThat(context.getBean(CustomConfigurationPropertiesMutabilityChecker.class))
                            .isExactlyInstanceOf(CustomConfigurationPropertiesMutabilityChecker.class);
                });
    }

    @TestConfiguration
    static class CustomConfigurationPropertiesConverterConfig {
        @Bean
        public ConfigurationPropertiesConverter configurationPropertiesConverter() {
            return new CustomConfigurationPropertiesConverter();
        }
    }

    @TestConfiguration
    static class CustomPropertyNameNormalizerConfig {
        @Bean
        public PropertyNameNormalizer propertyNameNormalizer() {
            return new CustomPropertyNameNormalizer();
        }
    }

    @TestConfiguration
    static class CustomSmartSanitizingFunctionConfig {
        @Bean
        public SmartSanitizingFunction smartSanitizingFunction(
                EndpointsConfigurationProperties endpointsConfigurationProperties,
                PropertyNameNormalizer propertyNameNormalizer) {
            return new CustomSmartSanitizingFunction(endpointsConfigurationProperties, propertyNameNormalizer);
        }
    }

    @TestConfiguration
    static class CustomConfigurationPropertiesCacheConfig {
        @Bean
        public ConfigurationPropertiesCache configurationPropertiesCache(
                SmartSanitizingFunction smartSanitizingFunction,
                ApplicationContext applicationContext,
                ConfigurationPropertiesConverter configurationPropertiesConverter) {
            return new CustomConfigurationPropertiesCache(
                    smartSanitizingFunction, applicationContext, configurationPropertiesConverter);
        }
    }

    @TestConfiguration
    static class CustomAxelixConfigurationPropertiesEndpointConfig {
        @Bean
        public AxelixConfigurationPropertiesEndpoint axelixConfigurationPropertiesEndpoint(
                ConfigurationPropertiesCache configurationPropertiesCache,
                ConfigurationPropertiesMutator configurationPropertiesMutator) {
            return new CustomAxelixConfigurationPropertiesEndpoint(
                    configurationPropertiesCache, configurationPropertiesMutator);
        }
    }

    @TestConfiguration
    static class CustomConfigurationPropertiesMutabilityCheckerConfig {

        @Bean
        public ConfigurationPropertiesMutabilityChecker configurationPropertiesMutabilityChecker() {
            return new CustomConfigurationPropertiesMutabilityChecker();
        }
    }

    @TestConfiguration
    static class CustomConfigurationPropertiesRuntimeValidatorConfig {

        @Bean
        public ConfigurationPropertiesRuntimeValidator configurationPropertiesRuntimeValidator() {
            return new CustomConfigurationPropertiesRuntimeValidator();
        }
    }

    @TestConfiguration
    static class CustomConfigurationPropertiesMutatorConfig {

        @Bean
        public CustomConfigurationPropertiesMutator configurationPropertiesMutator(
                ConfigurableEnvironment configurableEnvironment,
                PropertyNameDiscoverer propertyNameDiscoverer,
                ConfigurationPropertiesRuntimeValidator configurationPropertiesRuntimeValidator,
                ConfigurationPropertiesBeansCache configurationPropertiesBeansCache,
                ConfigurationPropertiesMutabilityChecker configurationPropertiesMutabilityChecker) {
            return new CustomConfigurationPropertiesMutator(
                    configurableEnvironment,
                    propertyNameDiscoverer,
                    configurationPropertiesRuntimeValidator,
                    configurationPropertiesBeansCache,
                    configurationPropertiesMutabilityChecker);
        }
    }

    static class CustomConfigurationPropertiesConverter implements ConfigurationPropertiesConverter {
        @Override
        public ConfigurationPropertiesFeed convert(
                ConfigurationPropertiesReportEndpoint.ConfigurationPropertiesDescriptor originalDescriptor) {
            return null;
        }
    }

    static class CustomPropertyNameNormalizer implements PropertyNameNormalizer {

        @Override
        public String normalize(String propertyName) {
            return "";
        }

        @Override
        public <C extends Collection<String>> C normalizeAll(C propertyNames, Supplier<C> collectionFactory) {
            return null;
        }
    }

    static class CustomSmartSanitizingFunction extends SmartSanitizingFunction {
        public CustomSmartSanitizingFunction(
                EndpointsConfigurationProperties endpointsConfigurationProperties,
                PropertyNameNormalizer propertyNameNormalizer) {
            super(endpointsConfigurationProperties.getSanitizedProperties(), propertyNameNormalizer);
        }
    }

    static class CustomConfigurationPropertiesMutabilityChecker extends ConfigurationPropertiesMutabilityChecker {
        public CustomConfigurationPropertiesMutabilityChecker() {
            super();
        }
    }

    static class CustomConfigurationPropertiesRuntimeValidator extends ConfigurationPropertiesRuntimeValidator {
        public CustomConfigurationPropertiesRuntimeValidator() {
            super();
        }
    }

    static class CustomConfigurationPropertiesCache extends ConfigurationPropertiesCache {
        public CustomConfigurationPropertiesCache(
                SmartSanitizingFunction smartSanitizingFunction,
                ApplicationContext applicationContext,
                ConfigurationPropertiesConverter configurationPropertiesConverter) {
            super(smartSanitizingFunction, applicationContext, configurationPropertiesConverter);
        }
    }

    static class CustomConfigurationPropertiesMutator extends RebindingConfigurationPropertiesMutator {
        public CustomConfigurationPropertiesMutator(
                ConfigurableEnvironment configurableEnvironment,
                PropertyNameDiscoverer propertyNameDiscoverer,
                ConfigurationPropertiesRuntimeValidator configurationPropertiesRuntimeValidator,
                ConfigurationPropertiesBeansCache configurationPropertiesBeansCache,
                ConfigurationPropertiesMutabilityChecker configurationPropertiesMutabilityChecker) {
            super(
                    configurableEnvironment,
                    propertyNameDiscoverer,
                    configurationPropertiesRuntimeValidator,
                    configurationPropertiesBeansCache,
                    configurationPropertiesMutabilityChecker);
        }
    }

    static class CustomAxelixConfigurationPropertiesEndpoint extends AxelixConfigurationPropertiesEndpoint {
        public CustomAxelixConfigurationPropertiesEndpoint(
                ConfigurationPropertiesCache configurationPropertiesCache,
                ConfigurationPropertiesMutator configurationPropertiesMutator) {
            super(configurationPropertiesCache, configurationPropertiesMutator);
        }
    }
}
