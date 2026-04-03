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
package com.axelixlabs.axelix.sbs.spring.core.configprops;

import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConfigurationPropertiesBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link ConfigurationPropertiesMutabilityChecker}.
 *
 * @author Nikita Kirillov
 */
@SpringBootTest(classes = ConfigurationPropertiesMutabilityCheckerTest.CurrentConfiguration.class)
@TestPropertySource(properties = {"axelix.prop.test.name=name"})
class ConfigurationPropertiesMutabilityCheckerTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private ConfigurationPropertiesMutabilityChecker mutabilityChecker;

    @ParameterizedTest
    @MethodSource("springConfigPropsBeanProvider")
    void allSpringConfigPropsBeansShouldBeFiltered(String beanName) {
        ConfigurationPropertiesBean bean =
                ConfigurationPropertiesBean.getAll(applicationContext).get(beanName);

        assertThat(bean).isNotNull();
        assertThat(mutabilityChecker.isNotMutable(bean)).isTrue();
    }

    static Stream<String> springConfigPropsBeanProvider() {
        return Stream.of(
                "dataSource",
                "spring.datasource-org.springframework.boot.autoconfigure.jdbc.DataSourceProperties",
                "spring.web-org.springframework.boot.autoconfigure.web.WebProperties",
                "spring.jackson-org.springframework.boot.autoconfigure.jackson.JacksonProperties",
                "spring.cloud.refresh-org.springframework.cloud.autoconfigure.RefreshAutoConfiguration$RefreshProperties",
                "management.info-org.springframework.boot.actuate.autoconfigure.info.InfoContributorProperties",
                "management.metrics-org.springframework.boot.actuate.autoconfigure.metrics.MetricsProperties",
                "spring.lifecycle-org.springframework.boot.autoconfigure.context.LifecycleProperties",
                "spring.kafka-org.springframework.boot.autoconfigure.kafka.KafkaProperties",
                "spring.cloud.openfeign.client-org.springframework.cloud.openfeign.FeignClientProperties");
    }

    @Test
    void shouldConsiderCustomConfigPropsBeanAsMutable() {
        ConfigurationPropertiesBean bean =
                ConfigurationPropertiesBean.getAll(applicationContext).get("axelixPropTest");

        assertThat(bean).isNotNull();
        assertThat(mutabilityChecker.isNotMutable(bean)).isFalse();
    }

    @TestConfiguration(value = "testCurrentConfiguration")
    @EnableConfigurationProperties(AxelixPropTest.class)
    static class CurrentConfiguration {

        @Bean
        public AxelixPropTest axelixPropTest() {
            return new AxelixPropTest();
        }

        @Bean
        public ConfigurationPropertiesMutabilityChecker mutabilityChecker() {
            return new ConfigurationPropertiesMutabilityChecker();
        }
    }

    @ConfigurationProperties(prefix = "axelix.prop.test")
    public static class AxelixPropTest {
        private String name;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}
