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

import java.time.Duration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;

import com.axelixlabs.axelix.common.auth.core.JwtAlgorithm;
import com.axelixlabs.axelix.common.auth.service.DefaultJwtEncoderService;
import com.axelixlabs.axelix.common.auth.service.JwtEncoderService;
import com.axelixlabs.axelix.sbs.spring.core.config.SelfRegistrationConfigurationProperties;
import com.axelixlabs.axelix.sbs.spring.core.master.DefaultSelfRegistrationMetadataAssembler;
import com.axelixlabs.axelix.sbs.spring.core.master.SelfRegistrationLifecycleListener;
import com.axelixlabs.axelix.sbs.spring.core.master.SelfRegistrationMetadataAssembler;
import com.axelixlabs.axelix.sbs.spring.core.master.SelfRegistrationService;
import com.axelixlabs.axelix.sbs.spring.core.master.ServiceMetadataAssembler;
import com.axelixlabs.axelix.sbs.spring.core.validate.ValidationListener;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

/**
 * Integration tests for {@link SelfRegistrationAutoConfiguration}
 *
 * @author Sergey Cherkasov
 */
class SelfRegistrationAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withPropertyValues("axelix.sbs.discovery.auto=true")
            .withUserConfiguration(RequiredDependenciesConfig.class)
            .withConfiguration(AutoConfigurations.of(SelfRegistrationAutoConfiguration.class));

    @Test
    void shouldCreateAllBeansInDefaultScenario() {
        contextRunner.run(context -> {
            assertThat(context).hasSingleBean(SelfRegistrationAutoConfiguration.class);
            assertThat(context).hasSingleBean(ValidationListener.class);
            assertThat(context).hasSingleBean(SelfRegistrationConfigurationProperties.class);
            assertThat(context).hasSingleBean(SelfRegistrationMetadataAssembler.class);
            assertThat(context).hasSingleBean(SelfRegistrationService.class);
            assertThat(context).hasSingleBean(SelfRegistrationLifecycleListener.class);
        });
    }

    @Test
    void shouldNotActivateAutoConfiguration_withoutRequiredProperty() {
        new ApplicationContextRunner()
                .withUserConfiguration(RequiredDependenciesConfig.class)
                .withConfiguration(AutoConfigurations.of(SelfRegistrationAutoConfiguration.class))
                .run(context -> {
                    assertThat(context).doesNotHaveBean(SelfRegistrationAutoConfiguration.class);
                    assertThat(context).doesNotHaveBean(SelfRegistrationConfigurationProperties.class);
                    assertThat(context).doesNotHaveBean(SelfRegistrationService.class);
                    assertThat(context).doesNotHaveBean(SelfRegistrationLifecycleListener.class);
                });
    }

    @Test
    void shouldNotActivateAutoConfiguration_whenAutoIsFalse() {
        new ApplicationContextRunner()
                .withPropertyValues("axelix.sbs.discovery.auto=false")
                .withUserConfiguration(RequiredDependenciesConfig.class)
                .withConfiguration(AutoConfigurations.of(SelfRegistrationAutoConfiguration.class))
                .run(context -> {
                    assertThat(context).doesNotHaveBean(SelfRegistrationAutoConfiguration.class);
                    assertThat(context).doesNotHaveBean(SelfRegistrationConfigurationProperties.class);
                    assertThat(context).doesNotHaveBean(SelfRegistrationService.class);
                    assertThat(context).doesNotHaveBean(SelfRegistrationLifecycleListener.class);
                });
    }

    @Test
    void shouldHandleMultipleCustomBeans() {
        contextRunner
                .withUserConfiguration(
                        CustomSelfRegistrationMetadataAssemblerConfig.class,
                        CustomSelfRegistrationLifecycleListenerConfig.class)
                .run(context -> {
                    assertThat(context.getBean(SelfRegistrationMetadataAssembler.class))
                            .isExactlyInstanceOf(CustomSelfRegistrationMetadataAssembler.class);
                    assertThat(context.getBean(SelfRegistrationLifecycleListener.class))
                            .isExactlyInstanceOf(CustomSelfRegistrationLifecycleListener.class);
                });
    }

    @TestConfiguration
    static class RequiredDependenciesConfig {

        @Bean
        public ServiceMetadataAssembler serviceMetadataAssembler() {
            return mock(ServiceMetadataAssembler.class);
        }

        @Bean
        public ObjectMapper objectMapper() {
            return new ObjectMapper();
        }

        @Bean
        public JwtEncoderService jwtEncoderService() {
            return new DefaultJwtEncoderService(JwtAlgorithm.HMAC512, "secret", Duration.ofHours(1));
        }
    }

    @TestConfiguration
    static class CustomSelfRegistrationMetadataAssemblerConfig {
        @Bean
        public SelfRegistrationMetadataAssembler selfRegistrationMetadataAssembler(
                ServiceMetadataAssembler serviceMetadataAssembler,
                SelfRegistrationConfigurationProperties selfRegistrationConfigurationProperties) {
            return new CustomSelfRegistrationMetadataAssembler(
                    serviceMetadataAssembler, selfRegistrationConfigurationProperties);
        }
    }

    @TestConfiguration
    static class CustomSelfRegistrationLifecycleListenerConfig {
        @Bean
        public SelfRegistrationLifecycleListener selfRegistrationLifecycleListener(
                SelfRegistrationService selfRegistrationService) {
            return new CustomSelfRegistrationLifecycleListener(selfRegistrationService);
        }
    }

    static class CustomSelfRegistrationMetadataAssembler extends DefaultSelfRegistrationMetadataAssembler {
        public CustomSelfRegistrationMetadataAssembler(
                ServiceMetadataAssembler serviceMetadataAssembler,
                SelfRegistrationConfigurationProperties selfRegistrationConfigurationProperties) {
            super(serviceMetadataAssembler, selfRegistrationConfigurationProperties);
        }
    }

    static class CustomSelfRegistrationLifecycleListener extends SelfRegistrationLifecycleListener {
        public CustomSelfRegistrationLifecycleListener(SelfRegistrationService selfRegistrationService) {
            super(selfRegistrationService);
        }
    }
}
