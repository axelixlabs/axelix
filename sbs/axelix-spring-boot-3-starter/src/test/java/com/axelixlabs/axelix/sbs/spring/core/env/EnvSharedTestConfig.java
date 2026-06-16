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
package com.axelixlabs.axelix.sbs.spring.core.env;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;

import com.axelixlabs.axelix.common.auth.core.SecurityContextExecutor;
import com.axelixlabs.axelix.sbs.spring.core.auth.RequiredAuthorityCheckService;
import com.axelixlabs.axelix.sbs.spring.core.auth.ThreadLocalSecurityContextExecutor;
import com.axelixlabs.axelix.sbs.spring.core.config.EndpointsConfigurationProperties;
import com.axelixlabs.axelix.sbs.spring.core.configprops.ConfigurationPropertiesConverter;
import com.axelixlabs.axelix.sbs.spring.core.configprops.ConfigurationPropertiesFlattener;
import com.axelixlabs.axelix.sbs.spring.core.configprops.ConfigurationPropertiesService;
import com.axelixlabs.axelix.sbs.spring.core.configprops.DefaultConfigurationPropertiesConverter;
import com.axelixlabs.axelix.sbs.spring.core.configprops.DefaultConfigurationPropertiesFlattener;
import com.axelixlabs.axelix.sbs.spring.core.configprops.DefaultConfigurationPropertiesService;
import com.axelixlabs.axelix.sbs.spring.core.configprops.SmartSanitizingFunction;

/**
 * Shared test configuration backing a single, cached Spring context for all non-endpoint {@code env}
 * integration tests. The two distinct sanitization behaviours required by these tests are exposed as two
 * separately-named {@link SmartSanitizingFunction} beans (and two matching {@link EnvironmentService} beans),
 * so a single context can satisfy every scenario.
 *
 * @author Nikita Kirillov
 * @author Mikhail Polivakha
 */
@TestConfiguration
public class EnvSharedTestConfig {

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
    public SecurityContextExecutor securityContextExecutor() {
        return new ThreadLocalSecurityContextExecutor();
    }

    @Bean
    public RequiredAuthorityCheckService requiredAuthorityCheckService(
            SecurityContextExecutor securityContextExecutor) {
        return new RequiredAuthorityCheckService(securityContextExecutor);
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
    @ConfigurationProperties(prefix = "axelix.sbs.endpoints.config")
    public EndpointsConfigurationProperties endpointsConfigurationProperties() {
        return new EndpointsConfigurationProperties();
    }

    /**
     * Declared {@code static} so the post-processor is registered early and analyzes every bean in the context
     * (including the {@code @Value} sample beans below).
     */
    @Bean
    public static ValueInjectionTrackerBeanPostProcessor valueInjectionTrackerBeanPostProcessor() {
        return new ValueInjectionTrackerBeanPostProcessor(new DefaultPropertyNameNormalizer());
    }

    @Bean
    public SmartSanitizingFunction sanitizeAllSmartSanitizingFunction(PropertyNameNormalizer propertyNameNormalizer) {
        return new SmartSanitizingFunction(EndpointsConfigurationProperties.SANITIZE_ALL, propertyNameNormalizer);
    }

    @Bean
    public SmartSanitizingFunction explicitSmartSanitizingFunction(PropertyNameNormalizer propertyNameNormalizer) {
        return new SmartSanitizingFunction(
                List.of("axelix.prop.test.tags.forSanitization", "axelix.prop.test.tags.FOR_SANITIZATION"),
                propertyNameNormalizer);
    }

    @Bean
    public ConfigurationPropertiesService configurationPropertiesService(
            @Qualifier("explicitSmartSanitizingFunction") SmartSanitizingFunction smartSanitizingFunction,
            ApplicationContext applicationContext,
            ConfigurationPropertiesConverter configurationPropertiesConverter,
            RequiredAuthorityCheckService requiredAuthorityCheckService) {
        return new DefaultConfigurationPropertiesService(
                smartSanitizingFunction,
                applicationContext,
                configurationPropertiesConverter,
                requiredAuthorityCheckService);
    }

    @Bean
    public EnvPropertyEnricher envPropertyEnricher(
            Environment environment,
            PropertyNameNormalizer propertyNameNormalizer,
            ObjectProvider<ConfigurationPropertiesService> configurationPropertiesServiceProvider,
            PropertyMetadataExtractor propertyMetadataExtractor,
            ValueInjectionTrackerBeanPostProcessor valueInjectionTrackerBeanPostProcessor) {
        return new DefaultEnvPropertyEnricher(
                environment,
                propertyNameNormalizer,
                configurationPropertiesServiceProvider,
                propertyMetadataExtractor,
                valueInjectionTrackerBeanPostProcessor);
    }

    @Bean
    public EnvironmentService sanitizeAllEnvironmentService(
            Environment environment,
            @Qualifier("sanitizeAllSmartSanitizingFunction") SmartSanitizingFunction smartSanitizingFunction,
            EnvPropertyEnricher envPropertyEnricher,
            RequiredAuthorityCheckService requiredAuthorityCheckService) {
        return new DefaultEnvironmentService(
                environment, smartSanitizingFunction, envPropertyEnricher, requiredAuthorityCheckService);
    }

    @Bean
    public EnvironmentService explicitSanitizeEnvironmentService(
            Environment environment,
            @Qualifier("explicitSmartSanitizingFunction") SmartSanitizingFunction smartSanitizingFunction,
            EnvPropertyEnricher envPropertyEnricher,
            RequiredAuthorityCheckService requiredAuthorityCheckService) {
        return new DefaultEnvironmentService(
                environment, smartSanitizingFunction, envPropertyEnricher, requiredAuthorityCheckService);
    }

    @Bean
    public TestBeanWithCustomAnnotations testBeanWithCustomAnnotations() {
        return new TestBeanWithCustomAnnotations("appName", "connectionTimeout");
    }

    @Bean
    public TestBeanWithSpEL testBeanWithSpEL() {
        return new TestBeanWithSpEL();
    }

    @ConfigurationProperties(prefix = "axelix.prop.test")
    public record AxelixConfigurationProperties(Map<String, String> tags) {}

    @Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD})
    @Retention(RetentionPolicy.RUNTIME)
    @Value("${test.app.timeout:5000}")
    public @interface TimeoutValue {}

    public static class TestBeanWithSpEL {

        @Value("#{environment.getProperty('server.port')}")
        private String envPort;

        @Value("#{systemProperties['user.home']}")
        private String systemHome;

        @Value("#{environment.getProperty('app.timeout')}")
        public Integer getTimeout() {
            return 5000;
        }
    }

    public static class TestBeanWithCustomAnnotations {

        @Value("${test.server.port:8080}")
        private String serverPort;

        @TimeoutValue
        private Integer timeout;

        public TestBeanWithCustomAnnotations(
                @Value("${test.spring.application.name:TestApp}") String appName,
                @TimeoutValue String connectionTimeout) {}

        private String profile;
        private Integer maxTimeout;

        @Autowired
        public void setProfile(@Value("${test.spring.profiles.active}") String profile) {
            this.profile = profile;
        }

        @Autowired
        public void setMaxTimeout(@TimeoutValue Integer timeout) {
            this.maxTimeout = timeout * 2;
        }

        @Value("${test.method.timeout}")
        public void calculateRandomTimeout() {}

        @TimeoutValue
        public void getDefaultTimeout() {}

        public String getServerPort() {
            return serverPort;
        }
    }
}
