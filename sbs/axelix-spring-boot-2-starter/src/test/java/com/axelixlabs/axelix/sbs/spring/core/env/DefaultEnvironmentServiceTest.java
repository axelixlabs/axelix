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
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

import com.axelixlabs.axelix.common.auth.core.DefaultSecurityContext;
import com.axelixlabs.axelix.common.auth.core.OssAuthority;
import com.axelixlabs.axelix.common.auth.core.SecurityContext;
import com.axelixlabs.axelix.common.auth.core.User;
import com.axelixlabs.axelix.sbs.spring.core.auth.ThreadLocalSecurityContextExecutor;
import com.axelixlabs.axelix.sbs.spring.core.config.EndpointsConfigurationProperties;
import com.axelixlabs.axelix.sbs.spring.core.configprops.SmartSanitizingFunction;
import com.axelixlabs.axelix.sbs.spring.core.contract.env.EnvironmentFeed;
import com.axelixlabs.axelix.sbs.spring.core.contract.env.Property;

import static com.axelixlabs.axelix.common.testfixtures.UserUtils.fromAuthorities;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link DefaultEnvironmentService}
 *
 * @author Nikita Kirillov
 */
class DefaultEnvironmentServiceTest {

    private static final ThreadLocalSecurityContextExecutor securityContextExecutor =
            new ThreadLocalSecurityContextExecutor();

    @SpringBootTest
    @TestPropertySource(
            properties = {
                "management.endpoint.env.keys-to-sanitize= ",
                "spring.jpa.show-sql=true",
                "server.error.includeStacktrace=always"
            })
    @Nested
    @Import(TestConfigWithAllPropertiesSanitized.class)
    class WithoutExplicitSanitizationProperties {

        @Autowired
        private EnvironmentService environmentService;

        @Test
        void shouldReturnSanitizedConfigurationProperties_whenRequiredAuthorityIsMissing() {
            // when.
            User user = fromAuthorities();
            SecurityContext securityContext = new DefaultSecurityContext(user, "testToken");
            EnvironmentFeed environmentFeed = securityContextExecutor.callWithinSecurityContext(
                    () -> environmentService.getEnvironmentFeed(null), securityContext);

            // then.
            Set<@Nullable String> values = environmentFeed.getPropertySources().stream()
                    .flatMap(propertySource -> propertySource.getProperties().stream())
                    .map(Property::getValue)
                    .collect(Collectors.toSet());

            assertThat(values).containsOnly("******");
            assertThat(environmentFeed).isNotNull().isInstanceOf(EnvironmentFeed.class);
        }

        @Test
        void shouldReturnUnsanitizedConfigurationProperties_whenUserHasRequiredAuthority() {
            // when.
            User user = fromAuthorities(OssAuthority.ENV_VALUES_READ);
            SecurityContext securityContext = new DefaultSecurityContext(user, "testToken");
            EnvironmentFeed environmentFeed = securityContextExecutor.callWithinSecurityContext(
                    () -> environmentService.getEnvironmentFeed(null), securityContext);

            // then.
            Set<@Nullable String> values = environmentFeed.getPropertySources().stream()
                    .flatMap(propertySource -> propertySource.getProperties().stream())
                    .map(Property::getValue)
                    .collect(Collectors.toSet());

            assertThat(values).doesNotContain("******");
            assertThat(environmentFeed).isNotNull().isInstanceOf(EnvironmentFeed.class);
        }

        @Test
        void shouldReportTheDangerousPropertyValue() {
            // when.
            User user = fromAuthorities(OssAuthority.ENV_VALUES_READ);
            SecurityContext securityContext = new DefaultSecurityContext(user, "testToken");
            EnvironmentFeed environmentFeed = securityContextExecutor.callWithinSecurityContext(
                    () -> environmentService.getEnvironmentFeed(null), securityContext);

            // then.
            Property ddlAuto = findPrimaryProperty(environmentFeed, "spring.jpa.hibernate.ddl-auto");

            assertThat(ddlAuto.getValue()).isEqualTo("create");
            assertThat(ddlAuto.getDangerousValue()).isNotNull();
            assertThat(ddlAuto.getDangerousValue().getRationale())
                    .isEqualTo(DangerousProperty.DDL_AUTO_CREATE.getRationale());
            assertThat(ddlAuto.getDangerousValue().getAlternativeExample()).isEqualTo("validate");
        }

        @Test
        void shouldNotReportAnythingForThePropertyWithAHarmlessValue() {
            // when.
            User user = fromAuthorities(OssAuthority.ENV_VALUES_READ);
            SecurityContext securityContext = new DefaultSecurityContext(user, "testToken");
            EnvironmentFeed environmentFeed = securityContextExecutor.callWithinSecurityContext(
                    () -> environmentService.getEnvironmentFeed(null), securityContext);

            // then.
            Property driverClassName = findPrimaryProperty(environmentFeed, "spring.datasource.driver-class-name");

            assertThat(driverClassName.getDangerousValue()).isNull();
        }

        @Test
        void shouldReportTheDangerousValueForThePropertySpelledInCamelCase() {
            // when.
            User user = fromAuthorities(OssAuthority.ENV_VALUES_READ);
            SecurityContext securityContext = new DefaultSecurityContext(user, "testToken");
            EnvironmentFeed environmentFeed = securityContextExecutor.callWithinSecurityContext(
                    () -> environmentService.getEnvironmentFeed(null), securityContext);

            // then.
            Property includeStacktrace = findPrimaryProperty(environmentFeed, "server.error.includeStacktrace");

            assertThat(includeStacktrace.getDangerousValue()).isNotNull();
            assertThat(includeStacktrace.getDangerousValue().getRationale())
                    .isEqualTo(DangerousProperty.INCLUDE_STACKTRACE_ALWAYS.getRationale());
        }

        @Test
        void shouldReportTheDangerousValueEvenWhenTheValueItselfIsSanitized() {
            // when.
            User user = fromAuthorities();
            SecurityContext securityContext = new DefaultSecurityContext(user, "testToken");
            EnvironmentFeed environmentFeed = securityContextExecutor.callWithinSecurityContext(
                    () -> environmentService.getEnvironmentFeed(null), securityContext);

            // then.
            Property ddlAuto = findPrimaryProperty(environmentFeed, "spring.jpa.hibernate.ddl-auto");

            assertThat(ddlAuto.getValue()).isEqualTo("******");
            assertThat(ddlAuto.getDangerousValue()).isNotNull();
            assertThat(ddlAuto.getDangerousValue().getRationale())
                    .isEqualTo(DangerousProperty.DDL_AUTO_CREATE.getRationale());
        }

        @Test
        void shouldReportTheDangerousValueOnlyForThePrimaryOccurrenceOfTheProperty() {
            // when.
            User user = fromAuthorities(OssAuthority.ENV_VALUES_READ);
            SecurityContext securityContext = new DefaultSecurityContext(user, "testToken");
            EnvironmentFeed environmentFeed = securityContextExecutor.callWithinSecurityContext(
                    () -> environmentService.getEnvironmentFeed(null), securityContext);

            // then. The very same property is declared both in application.yaml and in the test property source,
            // but only the one that actually wins is worth reporting.
            List<Property> showSql = findProperties(environmentFeed, "spring.jpa.show-sql");

            assertThat(showSql).hasSizeGreaterThan(1);

            assertThat(showSql)
                    .filteredOn(Property::getIsPrimary)
                    .singleElement()
                    .satisfies(
                            property -> assertThat(property.getDangerousValue()).isNotNull());

            assertThat(showSql)
                    .filteredOn(property -> !property.getIsPrimary())
                    .isNotEmpty()
                    .allSatisfy(
                            property -> assertThat(property.getDangerousValue()).isNull());
        }
    }

    @TestConfiguration
    @Import(EnvironmentTestConfig.class)
    static class TestConfigWithAllPropertiesSanitized {

        @Bean
        public SmartSanitizingFunction smartSanitizingFunction(PropertyNameNormalizer propertyNameNormalizer) {
            return new SmartSanitizingFunction(EndpointsConfigurationProperties.SANITIZE_ALL, propertyNameNormalizer);
        }
    }

    @SpringBootTest(
            properties = {
                "axelix.prop.test.tags.forSanitization=toBeSanitized",
                "axelix.prop.test.tags.FOR_SANITIZATION=toBeSanitized"
            })
    @Nested
    @Import(TestConfigWithExplicitSanitizationProperties.class)
    class WithExplicitSanitizationProperties {

        @Autowired
        private EnvironmentService environmentService;

        @Test
        void shouldReturnOnlyExplicitlySanitizedConfigurationProperties_whenRequiredAuthorityIsMissing() {
            // when.
            User user = fromAuthorities();
            SecurityContext securityContext = new DefaultSecurityContext(user, "testToken");
            EnvironmentFeed environmentFeed = securityContextExecutor.callWithinSecurityContext(
                    () -> environmentService.getEnvironmentFeed(null), securityContext);

            // then.
            Map<String, String> sanitizedProperties = environmentFeed.getPropertySources().stream()
                    .flatMap(propertySource -> propertySource.getProperties().stream())
                    .filter(property -> "******".equals(property.getValue()))
                    .collect(Collectors.toMap(Property::getName, Property::getValue));

            assertThat(sanitizedProperties)
                    .containsOnlyKeys("axelix.prop.test.tags.forSanitization", "axelix.prop.test.tags.FOR_SANITIZATION")
                    .containsValues("******", "******");

            assertThat(environmentFeed).isNotNull().isInstanceOf(EnvironmentFeed.class);
        }

        @Test
        void shouldReturnUnsanitizedConfigurationProperties_whenUserHasRequiredAuthority() {
            // when.
            User user = fromAuthorities(OssAuthority.ENV_VALUES_READ);
            SecurityContext securityContext = new DefaultSecurityContext(user, "testToken");
            EnvironmentFeed environmentFeed = securityContextExecutor.callWithinSecurityContext(
                    () -> environmentService.getEnvironmentFeed(null), securityContext);

            // then.
            Set<@Nullable String> values = environmentFeed.getPropertySources().stream()
                    .flatMap(propertySource -> propertySource.getProperties().stream())
                    .map(Property::getValue)
                    .collect(Collectors.toSet());

            assertThat(values).doesNotContain("******");
            assertThat(environmentFeed).isNotNull().isInstanceOf(EnvironmentFeed.class);
        }
    }

    @TestConfiguration
    @EnableConfigurationProperties(AxelixConfigurationProperties.class)
    @Import(EnvironmentTestConfig.class)
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

    private static Property findPrimaryProperty(EnvironmentFeed environmentFeed, String propertyName) {
        return findProperties(environmentFeed, propertyName).stream()
                .filter(Property::getIsPrimary)
                .findFirst()
                .orElseThrow();
    }

    private static List<Property> findProperties(EnvironmentFeed environmentFeed, String propertyName) {
        return environmentFeed.getPropertySources().stream()
                .flatMap(propertySource -> propertySource.getProperties().stream())
                .filter(property -> property.getName().equals(propertyName))
                .collect(Collectors.toList());
    }
}
