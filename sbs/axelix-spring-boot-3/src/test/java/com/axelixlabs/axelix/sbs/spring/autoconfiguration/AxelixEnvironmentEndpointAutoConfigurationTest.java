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

import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;

import org.springframework.boot.actuate.env.EnvironmentEndpoint;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

import com.axelixlabs.axelix.common.api.env.EnvironmentFeed;
import com.axelixlabs.axelix.sbs.spring.core.configprops.SmartSanitizingFunction;
import com.axelixlabs.axelix.sbs.spring.core.env.AxelixEnvironmentEndpoint;
import com.axelixlabs.axelix.sbs.spring.core.env.EnvPropertyEnricher;
import com.axelixlabs.axelix.sbs.spring.core.env.PropertyMetadata;
import com.axelixlabs.axelix.sbs.spring.core.env.PropertyMetadataExtractor;
import com.axelixlabs.axelix.sbs.spring.core.env.PropertyNameNormalizer;
import com.axelixlabs.axelix.sbs.spring.core.env.ValueInjectionTrackerBeanPostProcessor;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link AxelixEnvironmentEndpointAutoConfiguration}
 *
 * @since 09.02.2026
 * @author Nikita Kirillov
 * @author Mikhail Polivakha
 */
class AxelixEnvironmentEndpointAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withPropertyValues("management.endpoints.web.exposure.include=axelix-env")
            .withConfiguration(AutoConfigurations.of(AxelixEnvironmentEndpointAutoConfiguration.class));

    @Test
    void shouldCreateAllBeansInDefaultScenario() {
        contextRunner.run(context -> {
            assertThat(context).hasSingleBean(PropertyNameNormalizer.class);
            assertThat(context).hasSingleBean(PropertyMetadataExtractor.class);
            assertThat(context).hasSingleBean(SmartSanitizingFunction.class);
            assertThat(context).hasSingleBean(EnvPropertyEnricher.class);
            assertThat(context).hasSingleBean(AxelixEnvironmentEndpoint.class);
            assertThat(context).hasSingleBean(ValueInjectionTrackerBeanPostProcessor.class);
        });
    }

    @Test
    void shouldNotActivateAutoConfigurationWithoutRequiredProperty() {
        new ApplicationContextRunner()
                .withPropertyValues("management.endpoints.web.exposure.exclude=axelix-env")
                .withConfiguration(AutoConfigurations.of(AxelixEnvironmentEndpointAutoConfiguration.class))
                .run(context -> {
                    assertThat(context).doesNotHaveBean(AxelixEnvironmentEndpointAutoConfiguration.class);
                    assertThat(context).doesNotHaveBean(PropertyMetadataExtractor.class);
                    assertThat(context).doesNotHaveBean(EnvPropertyEnricher.class);
                    assertThat(context).doesNotHaveBean(AxelixEnvironmentEndpoint.class);
                    assertThat(context).doesNotHaveBean(ValueInjectionTrackerBeanPostProcessor.class);
                    assertThat(context).doesNotHaveBean(SmartSanitizingFunction.class);
                    assertThat(context).doesNotHaveBean(PropertyNameNormalizer.class);
                });
    }

    @Test
    void shouldHandleMultipleCustomBeans() {
        contextRunner
                .withUserConfiguration(
                        CustomPropertyMetadataExtractorConfig.class,
                        CustomEnvPropertyEnricherConfig.class,
                        CustomPropertyNameNormalizerConfig.class,
                        CustomAxelixEnvironmentEndpointConfig.class,
                        CustomValueInjectionTrackerBeanPostProcessorConfig.class)
                .run(context -> {
                    assertThat(context.getBean(PropertyMetadataExtractor.class))
                            .isExactlyInstanceOf(CustomPropertyMetadataExtractor.class);
                    assertThat(context.getBean(EnvPropertyEnricher.class))
                            .isExactlyInstanceOf(CustomEnvPropertyEnricher.class);
                    assertThat(context.getBean(PropertyNameNormalizer.class))
                            .isExactlyInstanceOf(CustomPropertyNameNormalizer.class);
                    assertThat(context.getBean(AxelixEnvironmentEndpoint.class))
                            .isExactlyInstanceOf(CustomAxelixEnvironmentEndpoint.class);
                    assertThat(context.getBean(ValueInjectionTrackerBeanPostProcessor.class))
                            .isExactlyInstanceOf(CustomValueInjectionTrackerBeanPostProcessor.class);
                });
    }

    @TestConfiguration
    static class CustomPropertyMetadataExtractorConfig {
        @Bean
        public PropertyMetadataExtractor propertyMetadataExtractor() {
            return new CustomPropertyMetadataExtractor();
        }
    }

    @TestConfiguration
    static class CustomEnvPropertyEnricherConfig {
        @Bean
        public EnvPropertyEnricher envPropertyEnricher() {
            return new CustomEnvPropertyEnricher();
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
    static class CustomAxelixEnvironmentEndpointConfig {
        @Bean
        public AxelixEnvironmentEndpoint axelixEnvironmentEndpoint(
                Environment environment,
                EnvPropertyEnricher envPropertyEnricher,
                SmartSanitizingFunction smartSanitizingFunction) {
            return new CustomAxelixEnvironmentEndpoint(environment, envPropertyEnricher, smartSanitizingFunction);
        }
    }

    @TestConfiguration
    static class CustomValueInjectionTrackerBeanPostProcessorConfig {
        @Bean
        public ValueInjectionTrackerBeanPostProcessor valueInjectionTrackerBeanPostProcessor() {
            return new CustomValueInjectionTrackerBeanPostProcessor(null);
        }
    }

    static class CustomPropertyMetadataExtractor implements PropertyMetadataExtractor {
        @Override
        public @Nullable PropertyMetadata getMetadata(String propertyName) {
            return null;
        }
    }

    static class CustomEnvPropertyEnricher implements EnvPropertyEnricher {
        @Override
        public EnvironmentFeed enrich(EnvironmentEndpoint.EnvironmentDescriptor originalDescriptor) {
            return null;
        }
    }

    static class CustomPropertyNameNormalizer implements PropertyNameNormalizer {
        @Override
        public String normalize(String propertyName) {
            return propertyName;
        }

        @Override
        public <C extends Collection<String>> C normalizeAll(C propertyNames, Supplier<C> collectionFactory) {
            return null;
        }
    }

    static class CustomAxelixEnvironmentEndpoint extends AxelixEnvironmentEndpoint {
        public CustomAxelixEnvironmentEndpoint(
                Environment environment,
                EnvPropertyEnricher envPropertyEnricher,
                SmartSanitizingFunction smartSanitizingFunction) {
            super(environment, smartSanitizingFunction, envPropertyEnricher);
        }
    }

    static class CustomValueInjectionTrackerBeanPostProcessor extends ValueInjectionTrackerBeanPostProcessor {
        public CustomValueInjectionTrackerBeanPostProcessor(PropertyNameNormalizer propertyNameNormalizer) {
            super(propertyNameNormalizer);
        }
    }
}
