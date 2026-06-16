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

import java.util.List;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

import com.axelixlabs.axelix.common.auth.core.SecurityContextExecutor;
import com.axelixlabs.axelix.sbs.spring.core.auth.RequiredAuthorityCheckService;
import com.axelixlabs.axelix.sbs.spring.core.auth.ThreadLocalSecurityContextExecutor;
import com.axelixlabs.axelix.sbs.spring.core.config.EndpointsConfigurationProperties;
import com.axelixlabs.axelix.sbs.spring.core.configprops.ConfigPropsTestSupportConfiguration.SharedAxelixConfigurationProperties;
import com.axelixlabs.axelix.sbs.spring.core.env.DefaultPropertyNameNormalizer;
import com.axelixlabs.axelix.sbs.spring.core.env.PropertyNameNormalizer;

/**
 * Shared test configuration for the {@code configprops} integration tests.
 *
 * <p>It provides the beans common to every configprops integration test and registers a single shared
 * {@link SharedAxelixConfigurationProperties} binding. The two {@link DefaultConfigurationPropertiesService}
 * beans differ only in their {@link SmartSanitizingFunction} configuration; declaring both in the same
 * context lets the tests select the desired sanitization behavior by qualifier while still sharing one
 * cached Spring context.
 *
 * @author Mikhail Polivakha
 * @author Sergey Cherkasov
 * @author Nikita Kirillov
 * @author Artemiy Degtyarev
 */
@TestConfiguration
@EnableConfigurationProperties(SharedAxelixConfigurationProperties.class)
public class ConfigPropsTestSupportConfiguration {

    @Bean
    public ConfigurationPropertiesFlattener configurationPropertiesFlattener() {
        return new DefaultConfigurationPropertiesFlattener();
    }

    @Bean
    public ConfigurationPropertiesConverter configurationPropertiesConverter(
            ConfigurationPropertiesFlattener configurationPropertiesFlattener) {
        return new DefaultConfigurationPropertiesConverter(configurationPropertiesFlattener);
    }

    @Bean
    public PropertyNameNormalizer propertyNameNormalizer() {
        return new DefaultPropertyNameNormalizer();
    }

    @Bean
    public SecurityContextExecutor securityContextExecutor() {
        return new ThreadLocalSecurityContextExecutor();
    }

    @Bean
    public RequiredAuthorityCheckService requiredAuthorityCheckService(
            SecurityContextExecutor securityContextExecutor) {
        return new RequiredAuthorityCheckService(securityContextExecutor);
    }

    @Bean
    public DefaultConfigurationPropertiesService sanitizeAllConfigurationPropertiesService(
            ApplicationContext applicationContext,
            ConfigurationPropertiesConverter configurationPropertiesConverter,
            RequiredAuthorityCheckService requiredAuthorityCheckService,
            PropertyNameNormalizer propertyNameNormalizer) {
        SmartSanitizingFunction smartSanitizingFunction =
                new SmartSanitizingFunction(EndpointsConfigurationProperties.SANITIZE_ALL, propertyNameNormalizer);
        return new DefaultConfigurationPropertiesService(
                smartSanitizingFunction,
                applicationContext,
                configurationPropertiesConverter,
                requiredAuthorityCheckService);
    }

    @Bean
    public DefaultConfigurationPropertiesService explicitlySanitizedConfigurationPropertiesService(
            ApplicationContext applicationContext,
            ConfigurationPropertiesConverter configurationPropertiesConverter,
            RequiredAuthorityCheckService requiredAuthorityCheckService,
            PropertyNameNormalizer propertyNameNormalizer) {
        SmartSanitizingFunction smartSanitizingFunction = new SmartSanitizingFunction(
                List.of("axelix.prop.test.tags.environment", "axelix.prop.test.tags.version"), propertyNameNormalizer);
        return new DefaultConfigurationPropertiesService(
                smartSanitizingFunction,
                applicationContext,
                configurationPropertiesConverter,
                requiredAuthorityCheckService);
    }

    @ConfigurationProperties(prefix = "axelix.prop.test")
    public record SharedAxelixConfigurationProperties(
            Map<String, String> tags, List<String> enabledContexts, HttpClient httpClient) {

        public record HttpClient(List<Request> requests) {}

        public record Request(String name, String baseUrl, List<Method> methods) {}

        public record Method(String type, List<Retry> retries) {}

        public record Retry(Integer count, Map<String, Object> parameters) {}
    }
}
