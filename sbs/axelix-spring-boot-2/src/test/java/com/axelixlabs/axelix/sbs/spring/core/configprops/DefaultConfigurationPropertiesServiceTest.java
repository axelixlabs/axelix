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
import java.util.Set;
import java.util.stream.Collectors;

import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

import com.axelixlabs.axelix.common.api.ConfigurationPropertiesFeed;
import com.axelixlabs.axelix.common.api.KeyValue;
import com.axelixlabs.axelix.common.auth.core.DefaultAuthority;
import com.axelixlabs.axelix.common.auth.core.DefaultSecurityContext;
import com.axelixlabs.axelix.common.auth.core.SecurityContext;
import com.axelixlabs.axelix.common.auth.core.SecurityContextExecutor;
import com.axelixlabs.axelix.common.auth.core.User;
import com.axelixlabs.axelix.sbs.spring.core.auth.RequiredAuthorityCheckService;
import com.axelixlabs.axelix.sbs.spring.core.auth.ThreadLocalSecurityContextExecutor;
import com.axelixlabs.axelix.sbs.spring.core.config.EndpointsConfigurationProperties;
import com.axelixlabs.axelix.sbs.spring.core.env.DefaultPropertyNameNormalizer;
import com.axelixlabs.axelix.sbs.spring.core.env.PropertyNameNormalizer;

import static com.axelixlabs.axelix.sbs.spring.core.utils.UserUtils.createUserWithAuthorities;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link DefaultConfigurationPropertiesService}.
 *
 * @since 13.11.2025
 * @author Sergey Cherkasov
 * @author Mikhail Polivakha
 * @author Nikita Kirillov
 */
public class DefaultConfigurationPropertiesServiceTest {

    private final ThreadLocalSecurityContextExecutor securityContextExecutor = new ThreadLocalSecurityContextExecutor();

    @SpringBootTest
    @Nested
    @Import(TestConfigWithAllPropertiesSanitized.class)
    class WithoutExplicitSanitizationProperties {

        @Autowired
        private ConfigurationPropertiesService configurationPropertiesService;

        @Test
        void shouldReturnSanitizedConfigurationProperties_whenRequiredAuthorityIsMissing() {
            // when.
            User user = createUserWithAuthorities();
            SecurityContext securityContext = new DefaultSecurityContext(user, "testToken");
            ConfigurationPropertiesFeed configProps = securityContextExecutor.callWithinSecurityContext(
                    () -> configurationPropertiesService.getConfigProps(), securityContext);

            // then.
            Set<@Nullable String> values = configProps.getBeans().stream()
                    .flatMap(bean -> bean.getProperties().stream())
                    .map(KeyValue::getValue)
                    .collect(Collectors.toSet());

            // TODO: Well, the "null" sanitization policy is not something that we currently have control over.
            // It is also not clear if we want to sanitize "null" values in general. I think
            // that it makes sense to sanitize them as well, but currently it is not possible due
            // to internal implementation of the Spring Boot Actuator native config props endpoint.
            assertThat(values).containsOnly(null, "******");
            assertThat(configProps).isNotNull().isInstanceOf(ConfigurationPropertiesFeed.class);
        }

        @Test
        void shouldReturnUnsanitizedConfigurationProperties_whenUserHasRequiredAuthority() {
            // when.
            User user = createUserWithAuthorities(DefaultAuthority.CONFIG_PROPS_VALUES_READ);
            SecurityContext securityContext = new DefaultSecurityContext(user, "testToken");
            ConfigurationPropertiesFeed configProps = securityContextExecutor.callWithinSecurityContext(
                    () -> configurationPropertiesService.getConfigProps(), securityContext);

            // then.
            Set<@Nullable String> values = configProps.getBeans().stream()
                    .flatMap(bean -> bean.getProperties().stream())
                    .map(KeyValue::getValue)
                    .collect(Collectors.toSet());

            assertThat(values).doesNotContain("******");
            assertThat(configProps).isNotNull().isInstanceOf(ConfigurationPropertiesFeed.class);
        }
    }

    @SpringBootTest(
            properties = {
                "axelix.prop.test.tags.forSanitization=toBeSanitized",
                "axelix.prop.test.tags.FOR_SANITIZATION=toBeSanitized"
            })
    @Nested
    @EnableConfigurationProperties(AxelixConfigurationProperties.class)
    @Import(TestConfigWithExplicitSanitizationProperties.class)
    class WithExplicitSanitizationProperties {

        @Autowired
        private ConfigurationPropertiesService configurationPropertiesService;

        @Test
        void shouldReturnOnlyExplicitlySanitizedConfigurationProperties_whenRequiredAuthorityIsMissing() {
            // when.
            User user = createUserWithAuthorities();
            SecurityContext securityContext = new DefaultSecurityContext(user, "testToken");
            ConfigurationPropertiesFeed configProps = securityContextExecutor.callWithinSecurityContext(
                    () -> configurationPropertiesService.getConfigProps(), securityContext);

            // then.
            Map<String, String> sanitizedProperties = configProps.getBeans().stream()
                    .flatMap(bean -> bean.getProperties().stream())
                    .filter(prop -> "******".equals(prop.getValue()))
                    .collect(Collectors.toMap(KeyValue::getKey, KeyValue::getValue));

            assertThat(sanitizedProperties)
                    .containsOnlyKeys("tags.forSanitization", "tags.FOR_SANITIZATION")
                    .containsValues("******", "******");

            List<KeyValue> nonSanitizedProps = configProps.getBeans().stream()
                    .flatMap(bean -> bean.getProperties().stream())
                    .filter(prop -> !"******".equals(prop.getValue()))
                    .filter(prop -> prop.getValue() != null)
                    .collect(Collectors.toList());

            assertThat(nonSanitizedProps)
                    .allMatch(prop -> !prop.getKey().contains("forSanitization")
                            && !prop.getKey().contains("FOR_SANITIZATION"));

            assertThat(configProps).isNotNull().isInstanceOf(ConfigurationPropertiesFeed.class);
        }

        @Test
        void shouldReturnUnsanitizedConfigurationProperties_whenUserHasRequiredAuthority() {
            // when.
            User user = createUserWithAuthorities(DefaultAuthority.CONFIG_PROPS_VALUES_READ);
            SecurityContext securityContext = new DefaultSecurityContext(user, "testToken");
            ConfigurationPropertiesFeed configProps = securityContextExecutor.callWithinSecurityContext(
                    () -> configurationPropertiesService.getConfigProps(), securityContext);

            // then.
            Set<@Nullable String> values = configProps.getBeans().stream()
                    .flatMap(bean -> bean.getProperties().stream())
                    .map(KeyValue::getValue)
                    .collect(Collectors.toSet());

            assertThat(values).doesNotContain("******");
            assertThat(configProps).isNotNull().isInstanceOf(ConfigurationPropertiesFeed.class);
        }
    }

    @TestConfiguration
    @Import(ConfigurationPropertiesServiceTestConfiguration.class)
    static class TestConfigWithAllPropertiesSanitized {

        @Bean
        public SmartSanitizingFunction smartSanitizingFunction(PropertyNameNormalizer propertyNameNormalizer) {
            return new SmartSanitizingFunction(List.of("*"), propertyNameNormalizer);
        }
    }

    @TestConfiguration
    @Import(ConfigurationPropertiesServiceTestConfiguration.class)
    static class TestConfigWithExplicitSanitizationProperties {

        @Bean
        public SmartSanitizingFunction smartSanitizingFunction(PropertyNameNormalizer propertyNameNormalizer) {
            return new SmartSanitizingFunction(
                    List.of("axelix.prop.test.tags.forSanitization", "axelix.prop.test.tags.FOR_SANITIZATION"),
                    propertyNameNormalizer);
        }
    }

    @ConfigurationProperties(prefix = "axelix.prop.test")
    static class AxelixConfigurationProperties {

        private Map<String, String> tags;

        public Map<String, String> getTags() {
            return tags;
        }

        public void setTags(Map<String, String> tags) {
            this.tags = tags;
        }
    }

    static class ConfigurationPropertiesServiceTestConfiguration {

        @Bean
        @ConfigurationProperties(prefix = "axelix.sbs.endpoints.config")
        public EndpointsConfigurationProperties endpointsConfigurationProperties() {
            return new EndpointsConfigurationProperties();
        }

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
        public ConfigurationPropertiesService configurationPropertiesService(
                SmartSanitizingFunction smartSanitizingFunction,
                ApplicationContext applicationContext,
                ConfigurationPropertiesConverter configurationPropertiesConverter,
                RequiredAuthorityCheckService requiredAuthorityCheckService) {
            return new DefaultConfigurationPropertiesService(
                    smartSanitizingFunction,
                    applicationContext,
                    configurationPropertiesConverter,
                    requiredAuthorityCheckService);
        }
    }
}
