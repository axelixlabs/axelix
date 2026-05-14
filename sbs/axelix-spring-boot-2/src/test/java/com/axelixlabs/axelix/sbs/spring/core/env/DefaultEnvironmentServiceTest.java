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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;

import com.axelixlabs.axelix.common.api.env.EnvironmentFeed;
import com.axelixlabs.axelix.common.api.env.EnvironmentFeed.Property;
import com.axelixlabs.axelix.common.auth.core.DefaultAuthority;
import com.axelixlabs.axelix.common.auth.core.DefaultSecurityContext;
import com.axelixlabs.axelix.common.auth.core.SecurityContext;
import com.axelixlabs.axelix.common.auth.core.User;
import com.axelixlabs.axelix.sbs.spring.core.auth.RequiredAuthorityCheckService;
import com.axelixlabs.axelix.sbs.spring.core.auth.ThreadLocalSecurityContextExecutor;
import com.axelixlabs.axelix.sbs.spring.core.config.EndpointsConfigurationProperties;
import com.axelixlabs.axelix.sbs.spring.core.configprops.SmartSanitizingFunction;
import com.axelixlabs.axelix.sbs.spring.core.shared.AbstractEndpointTest;

import static com.axelixlabs.axelix.sbs.spring.core.utils.UserUtils.createUserWithAuthorities;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link DefaultEnvironmentService}.
 *
 * <p>Each nested class autowires the raw dependencies from the shared endpoint context
 * and constructs its own {@link DefaultEnvironmentService} instance with the
 * {@link SmartSanitizingFunction} configuration it wants to exercise. This avoids
 * spinning up a dedicated {@code ApplicationContext} per sanitization scheme.
 *
 * @author Nikita Kirillov
 */
class DefaultEnvironmentServiceTest extends AbstractEndpointTest {

    private static final ThreadLocalSecurityContextExecutor securityContextExecutor =
            new ThreadLocalSecurityContextExecutor();

    @Autowired
    private Environment environment;

    @Autowired
    private EnvPropertyEnricher envPropertyEnricher;

    @Autowired
    private RequiredAuthorityCheckService requiredAuthorityCheckService;

    @Autowired
    private PropertyNameNormalizer propertyNameNormalizer;

    @Nested
    class WithoutExplicitSanitizationProperties {

        private DefaultEnvironmentService environmentService;

        @BeforeEach
        void setUp() {
            SmartSanitizingFunction sanitizingFunction = new SmartSanitizingFunction(
                    EndpointsConfigurationProperties.SANITIZE_ALL, propertyNameNormalizer);
            environmentService = new DefaultEnvironmentService(
                    environment, sanitizingFunction, envPropertyEnricher, requiredAuthorityCheckService);
        }

        @Test
        void shouldReturnSanitizedConfigurationProperties_whenRequiredAuthorityIsMissing() {
            // when.
            User user = createUserWithAuthorities();
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
            User user = createUserWithAuthorities(DefaultAuthority.ENV_VALUES_READ);
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

    @Nested
    class WithExplicitSanitizationProperties {

        private DefaultEnvironmentService environmentService;

        @BeforeEach
        void setUp() {
            SmartSanitizingFunction sanitizingFunction = new SmartSanitizingFunction(
                    List.of("axelix.prop.test.tags.forSanitization", "axelix.prop.test.tags.FOR_SANITIZATION"),
                    propertyNameNormalizer);
            environmentService = new DefaultEnvironmentService(
                    environment, sanitizingFunction, envPropertyEnricher, requiredAuthorityCheckService);
        }

        @Test
        void shouldReturnOnlyExplicitlySanitizedConfigurationProperties_whenRequiredAuthorityIsMissing() {
            // when.
            User user = createUserWithAuthorities();
            SecurityContext securityContext = new DefaultSecurityContext(user, "testToken");
            EnvironmentFeed environmentFeed = securityContextExecutor.callWithinSecurityContext(
                    () -> environmentService.getEnvironmentFeed(null), securityContext);

            // then.
            Map<String, String> sanitizedProperties = environmentFeed.getPropertySources().stream()
                    .flatMap(propertySource -> propertySource.getProperties().stream())
                    .filter(property -> "******".equals(property.getValue()))
                    .collect(Collectors.toMap(Property::getName, Property::getValue, (a, b) -> a));

            assertThat(sanitizedProperties)
                    .containsOnlyKeys("axelix.prop.test.tags.forSanitization", "axelix.prop.test.tags.FOR_SANITIZATION")
                    .containsValues("******", "******");

            assertThat(environmentFeed).isNotNull().isInstanceOf(EnvironmentFeed.class);
        }

        @Test
        void shouldReturnUnsanitizedConfigurationProperties_whenUserHasRequiredAuthority() {
            // when.
            User user = createUserWithAuthorities(DefaultAuthority.ENV_VALUES_READ);
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
}
