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

import com.axelixlabs.axelix.common.api.ConfigurationPropertiesFeed;
import com.axelixlabs.axelix.common.auth.core.SecurityContextExecutor;
import com.axelixlabs.axelix.sbs.spring.core.auth.RequiredAuthorityCheckService;
import com.axelixlabs.axelix.sbs.spring.core.auth.ThreadLocalSecurityContextExecutor;
import com.axelixlabs.axelix.sbs.spring.core.config.EndpointsConfigurationProperties;
import com.axelixlabs.axelix.sbs.spring.core.configprops.AxelixConfigurationPropertiesEndpoint;
import com.axelixlabs.axelix.sbs.spring.core.configprops.ConfigurationPropertiesConverter;
import com.axelixlabs.axelix.sbs.spring.core.configprops.ConfigurationPropertiesFlattener;
import com.axelixlabs.axelix.sbs.spring.core.configprops.ConfigurationPropertiesService;
import com.axelixlabs.axelix.sbs.spring.core.configprops.DefaultConfigurationPropertiesFlattener;
import com.axelixlabs.axelix.sbs.spring.core.configprops.DefaultConfigurationPropertiesService;
import com.axelixlabs.axelix.sbs.spring.core.configprops.SmartSanitizingFunction;
import com.axelixlabs.axelix.sbs.spring.core.env.PropertyNameNormalizer;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test for {@link AxelixConfigurationsPropertiesEndpointAutoConfiguration}
 *
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
            assertThat(context).hasSingleBean(ConfigurationPropertiesFlattener.class);
            assertThat(context).hasSingleBean(ConfigurationPropertiesConverter.class);
            assertThat(context).hasSingleBean(PropertyNameNormalizer.class);
            assertThat(context).hasSingleBean(SmartSanitizingFunction.class);
            assertThat(context).hasSingleBean(ConfigurationPropertiesService.class);
            assertThat(context).hasSingleBean(AxelixConfigurationPropertiesEndpoint.class);
            assertThat(context).hasSingleBean(RequiredAuthorityCheckService.class);
            assertThat(context).hasSingleBean(SecurityContextExecutor.class);
        });
    }

    @Test
    void shouldNotActivateAutoConfigurationWhenEndpointDisabled() {
        contextRunner // Overriding the property value to test the disabled state
                .withPropertyValues("management.endpoints.web.exposure.exclude=axelix-configprops")
                .run(context -> {
                    assertThat(context).doesNotHaveBean(AxelixConfigurationsPropertiesEndpointAutoConfiguration.class);
                    assertThat(context).doesNotHaveBean(AxelixConfigurationPropertiesEndpoint.class);
                    assertThat(context).doesNotHaveBean(ConfigurationPropertiesService.class);
                    assertThat(context).doesNotHaveBean(SmartSanitizingFunction.class);
                    assertThat(context).doesNotHaveBean(PropertyNameNormalizer.class);
                    assertThat(context).doesNotHaveBean(ConfigurationPropertiesConverter.class);
                    assertThat(context).doesNotHaveBean(ConfigurationPropertiesFlattener.class);
                    assertThat(context).doesNotHaveBean(SecurityContextExecutor.class);
                    assertThat(context).doesNotHaveBean(RequiredAuthorityCheckService.class);
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
            assertThat(context).doesNotHaveBean(ConfigurationPropertiesService.class);
            assertThat(context).doesNotHaveBean(SmartSanitizingFunction.class);
            assertThat(context).doesNotHaveBean(PropertyNameNormalizer.class);
            assertThat(context).doesNotHaveBean(ConfigurationPropertiesConverter.class);
            assertThat(context).doesNotHaveBean(ConfigurationPropertiesFlattener.class);
            assertThat(context).doesNotHaveBean(SecurityContextExecutor.class);
            assertThat(context).doesNotHaveBean(RequiredAuthorityCheckService.class);
        });
    }

    @Test
    void shouldHandleMultipleCustomBeans() {
        contextRunner
                .withUserConfiguration(
                        CustomConfigurationPropertiesConverterConfig.class,
                        CustomConfigurationPropertiesFlattenerConfig.class,
                        CustomPropertyNameNormalizerConfig.class,
                        CustomSmartSanitizingFunctionConfig.class,
                        CustomConfigurationPropertiesServiceConfig.class,
                        CustomAxelixConfigurationPropertiesEndpointConfig.class,
                        CustomSecurityContextExecutorConfig.class,
                        CustomRequiredAuthorityCheckService.class)
                .run(context -> {
                    assertThat(context.getBean(ConfigurationPropertiesConverter.class))
                            .isExactlyInstanceOf(CustomConfigurationPropertiesConverter.class);
                    assertThat(context.getBean(PropertyNameNormalizer.class))
                            .isExactlyInstanceOf(CustomPropertyNameNormalizer.class);
                    assertThat(context.getBean(SmartSanitizingFunction.class))
                            .isExactlyInstanceOf(CustomSmartSanitizingFunction.class);
                    assertThat(context.getBean(DefaultConfigurationPropertiesService.class))
                            .isExactlyInstanceOf(CustomConfigurationPropertiesService.class);
                    assertThat(context.getBean(AxelixConfigurationPropertiesEndpoint.class))
                            .isExactlyInstanceOf(CustomAxelixConfigurationPropertiesEndpoint.class);
                    assertThat(context.getBean(ConfigurationPropertiesFlattener.class))
                            .isExactlyInstanceOf(CustomConfigurationPropertiesFlattener.class);
                    assertThat(context.getBean(SecurityContextExecutor.class))
                            .isExactlyInstanceOf(CustomSecurityContextExecutor.class);
                    assertThat(context.getBean(RequiredAuthorityCheckService.class))
                            .isExactlyInstanceOf(CustomRequiredAuthorityCheckService.class);
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
    static class CustomConfigurationPropertiesServiceConfig {
        @Bean
        public ConfigurationPropertiesService configurationPropertiesService(
                SmartSanitizingFunction smartSanitizingFunction,
                ApplicationContext applicationContext,
                ConfigurationPropertiesConverter configurationPropertiesConverter,
                RequiredAuthorityCheckService requiredAuthorityCheckService) {
            return new CustomConfigurationPropertiesService(
                    smartSanitizingFunction,
                    applicationContext,
                    configurationPropertiesConverter,
                    requiredAuthorityCheckService);
        }
    }

    @TestConfiguration
    static class CustomAxelixConfigurationPropertiesEndpointConfig {
        @Bean
        public AxelixConfigurationPropertiesEndpoint axelixConfigurationPropertiesEndpoint(
                DefaultConfigurationPropertiesService configurationPropertiesService) {
            return new CustomAxelixConfigurationPropertiesEndpoint(configurationPropertiesService);
        }
    }

    @TestConfiguration
    static class CustomConfigurationPropertiesFlattenerConfig {
        @Bean
        public ConfigurationPropertiesFlattener configurationPropertiesFlattener() {
            return new CustomConfigurationPropertiesFlattener();
        }
    }

    @TestConfiguration
    static class CustomSecurityContextExecutorConfig {
        @Bean
        public SecurityContextExecutor securityContextExecutor() {
            return new CustomSecurityContextExecutor();
        }
    }

    @TestConfiguration
    static class CustomRequiredAuthorityCheckServiceConfig {
        @Bean
        public RequiredAuthorityCheckService configurationPropertiesFlattener(
                SecurityContextExecutor securityContextExecutor) {
            return new CustomRequiredAuthorityCheckService(securityContextExecutor);
        }
    }

    static class CustomConfigurationPropertiesConverter implements ConfigurationPropertiesConverter {
        @Override
        public ConfigurationPropertiesFeed convert(
                ConfigurationPropertiesReportEndpoint.ConfigurationPropertiesDescriptor originalDescriptor) {
            return null;
        }
    }

    static class CustomSecurityContextExecutor extends ThreadLocalSecurityContextExecutor {}

    static class CustomRequiredAuthorityCheckService extends RequiredAuthorityCheckService {
        public CustomRequiredAuthorityCheckService(SecurityContextExecutor securityContextExecutor) {
            super(securityContextExecutor);
        }
    }

    static class CustomConfigurationPropertiesFlattener extends DefaultConfigurationPropertiesFlattener {}

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

    static class CustomConfigurationPropertiesService extends DefaultConfigurationPropertiesService {
        public CustomConfigurationPropertiesService(
                SmartSanitizingFunction smartSanitizingFunction,
                ApplicationContext applicationContext,
                ConfigurationPropertiesConverter configurationPropertiesConverter,
                RequiredAuthorityCheckService requiredAuthorityCheckService) {
            super(
                    smartSanitizingFunction,
                    applicationContext,
                    configurationPropertiesConverter,
                    requiredAuthorityCheckService);
        }
    }

    static class CustomAxelixConfigurationPropertiesEndpoint extends AxelixConfigurationPropertiesEndpoint {
        public CustomAxelixConfigurationPropertiesEndpoint(
                DefaultConfigurationPropertiesService configurationPropertiesService) {
            super(configurationPropertiesService);
        }
    }
}
