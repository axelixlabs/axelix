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

import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;

import com.axelixlabs.axelix.sbs.spring.core.beans.AxelixBeansEndpoint;
import com.axelixlabs.axelix.sbs.spring.core.beans.BeanMetaInfo;
import com.axelixlabs.axelix.sbs.spring.core.beans.BeanMetaInfoExtractor;
import com.axelixlabs.axelix.sbs.spring.core.beans.BeansFeedBuilder;
import com.axelixlabs.axelix.sbs.spring.core.beans.CachingBeansFeedBuilder;
import com.axelixlabs.axelix.sbs.spring.core.beans.DefaultBeansFeedBuilder;
import com.axelixlabs.axelix.sbs.spring.core.beans.QualifiersPersistencePostProcessor;
import com.axelixlabs.axelix.sbs.spring.core.conditions.ConditionalBeanRefBuilder;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test for {@link AxelixBeansEndpointAutoConfiguration}
 *
 * @since 09.02.2026
 * @author Nikita Kirillov
 */
class AxelixBeansEndpointAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withPropertyValues("management.endpoints.web.exposure.include=axelix-beans")
            .withConfiguration(AutoConfigurations.of(AxelixBeansEndpointAutoConfiguration.class));

    @Test
    void shouldCreateAllBeansInDefaultScenario() {
        contextRunner.run(context -> {
            assertThat(context).hasSingleBean(ConditionalBeanRefBuilder.class);
            assertThat(context).hasSingleBean(BeanMetaInfoExtractor.class);
            assertThat(context).hasSingleBean(AxelixBeansEndpoint.class);
            assertThat(context).hasSingleBean(QualifiersPersistencePostProcessor.class);

            assertThat(context).getBeans(BeansFeedBuilder.class).hasSize(2);
            assertThat(context).getBean("defaultBeansFeedBuilder").isExactlyInstanceOf(DefaultBeansFeedBuilder.class);
            assertThat(context).getBean("cachingBeansFeedBuilder").isExactlyInstanceOf(CachingBeansFeedBuilder.class);
        });
    }

    @Test
    void shouldNotActivateAutoConfigurationWhenEndpointDisabled() {
        contextRunner // Overriding the property value to test the disabled state
                .withPropertyValues("management.endpoints.web.exposure.exclude=axelix-beans")
                .run(context -> {
                    assertThat(context).doesNotHaveBean(AxelixBeansEndpointAutoConfiguration.class);
                    assertThat(context).doesNotHaveBean(AxelixBeansEndpoint.class);
                    assertThat(context).doesNotHaveBean(ConditionalBeanRefBuilder.class);
                    assertThat(context).doesNotHaveBean(BeanMetaInfoExtractor.class);
                    assertThat(context).doesNotHaveBean(BeansFeedBuilder.class);
                    assertThat(context).doesNotHaveBean(QualifiersPersistencePostProcessor.class);
                });
    }

    @Test
    void shouldNotActivateAutoConfigurationWithoutRequiredProperty() {
        ApplicationContextRunner runnerWithoutCacheConfig = new ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(AxelixBeansEndpointAutoConfiguration.class));

        runnerWithoutCacheConfig.run(context -> {
            assertThat(context).doesNotHaveBean(AxelixBeansEndpointAutoConfiguration.class);
            assertThat(context).doesNotHaveBean(AxelixBeansEndpoint.class);
            assertThat(context).doesNotHaveBean(ConditionalBeanRefBuilder.class);
            assertThat(context).doesNotHaveBean(BeanMetaInfoExtractor.class);
            assertThat(context).doesNotHaveBean(BeansFeedBuilder.class);
            assertThat(context).doesNotHaveBean(QualifiersPersistencePostProcessor.class);
        });
    }

    @Test
    void shouldHandleMultipleCustomBeans() {
        contextRunner
                .withUserConfiguration(
                        CustomConditionalBeanRefBuilderConfig.class,
                        CustomBeanMetaInfoExtractorConfig.class,
                        CustomAxelixBeansEndpointConfig.class)
                .run(context -> {
                    assertThat(context.getBean(ConditionalBeanRefBuilder.class))
                            .isExactlyInstanceOf(CustomConditionalBeanRefBuilder.class);
                    assertThat(context.getBean(BeanMetaInfoExtractor.class))
                            .isExactlyInstanceOf(CustomBeanMetaInfoExtractor.class);
                    assertThat(context.getBean(AxelixBeansEndpoint.class))
                            .isExactlyInstanceOf(CustomAxelixBeansEndpoint.class);
                });
    }

    @TestConfiguration
    static class CustomConditionalBeanRefBuilderConfig {
        @Bean
        public ConditionalBeanRefBuilder conditionalBeanRefBuilder() {
            return new CustomConditionalBeanRefBuilder();
        }
    }

    @TestConfiguration
    static class CustomBeanMetaInfoExtractorConfig {
        @Bean
        public BeanMetaInfoExtractor beanMetaInfoExtractor() {
            return new CustomBeanMetaInfoExtractor();
        }
    }

    @TestConfiguration
    static class CustomAxelixBeansEndpointConfig {
        @Bean
        public AxelixBeansEndpoint axelixBeansEndpoint() {
            return new CustomAxelixBeansEndpoint();
        }
    }

    static class CustomConditionalBeanRefBuilder implements ConditionalBeanRefBuilder {
        @Override
        public String buildBeanRefInternal(Class<?> beanClass, @Nullable String beanFactoryMethodName) {
            return "";
        }
    }

    static class CustomBeanMetaInfoExtractor implements BeanMetaInfoExtractor {
        @Override
        public BeanMetaInfo extract(String beanName, ConfigurableListableBeanFactory beanFactory) {
            return null;
        }
    }

    static class CustomAxelixBeansEndpoint extends AxelixBeansEndpoint {
        public CustomAxelixBeansEndpoint() {
            super(null);
        }
    }
}
