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
import org.springframework.beans.factory.annotation.Qualifier;

import com.axelixlabs.axelix.common.api.ConfigurationPropertiesFeed;
import com.axelixlabs.axelix.common.api.KeyValue;
import com.axelixlabs.axelix.common.auth.core.DefaultAuthority;
import com.axelixlabs.axelix.common.auth.core.DefaultSecurityContext;
import com.axelixlabs.axelix.common.auth.core.SecurityContext;
import com.axelixlabs.axelix.common.auth.core.User;
import com.axelixlabs.axelix.sbs.spring.core.auth.ThreadLocalSecurityContextExecutor;

import static com.axelixlabs.axelix.sbs.spring.core.configprops.ConfigPropsTestSupportConfiguration.EXPLICITLY_SANITIZED_CONFIGURATION_PROPERTIES_SERVICE;
import static com.axelixlabs.axelix.sbs.spring.core.configprops.ConfigPropsTestSupportConfiguration.SANITIZE_ALL_CONFIGURATION_PROPERTIES_SERVICE;
import static com.axelixlabs.axelix.sbs.spring.core.utils.UserUtils.createUserWithAuthorities;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link DefaultConfigurationPropertiesService}.
 *
 * @since 13.11.2025
 * @author Sergey Cherkasov
 * @author Mikhail Polivakha
 * @author Nikita Kirillov
 * @author Artemiy Degtyarev
 */
public class DefaultConfigurationPropertiesServiceTest extends AbstractConfigPropsSharedContextTest {

    private final ThreadLocalSecurityContextExecutor securityContextExecutor = new ThreadLocalSecurityContextExecutor();

    @Nested
    class WithoutExplicitSanitizationProperties {

        @Autowired
        @Qualifier(SANITIZE_ALL_CONFIGURATION_PROPERTIES_SERVICE)
        private ConfigurationPropertiesService configurationPropertiesService;

        @Test
        void shouldReturnSanitizedConfigurationProperties_whenRequiredAuthorityIsMissing() {
            // when.
            User user = createUserWithAuthorities();
            SecurityContext securityContext = new DefaultSecurityContext(user, "testToken");
            ConfigurationPropertiesFeed configProps = securityContextExecutor.callWithinSecurityContext(
                    configurationPropertiesService::getConfigProps, securityContext);

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
        }

        @Test
        void shouldReturnUnsanitizedConfigurationProperties_whenUserHasRequiredAuthority() {
            // when.
            User user = createUserWithAuthorities(DefaultAuthority.CONFIG_PROPS_VALUES_READ);
            SecurityContext securityContext = new DefaultSecurityContext(user, "testToken");
            ConfigurationPropertiesFeed configProps = securityContextExecutor.callWithinSecurityContext(
                    configurationPropertiesService::getConfigProps, securityContext);

            // then.
            Set<@Nullable String> values = configProps.getBeans().stream()
                    .flatMap(bean -> bean.getProperties().stream())
                    .map(KeyValue::getValue)
                    .collect(Collectors.toSet());

            assertThat(values).doesNotContain("******");
        }
    }

    @Nested
    class WithExplicitSanitizationProperties {

        @Autowired
        @Qualifier(EXPLICITLY_SANITIZED_CONFIGURATION_PROPERTIES_SERVICE)
        private ConfigurationPropertiesService configurationPropertiesService;

        @Test
        void shouldReturnOnlyExplicitlySanitizedConfigurationProperties_whenRequiredAuthorityIsMissing() {
            // when.
            User user = createUserWithAuthorities();
            SecurityContext securityContext = new DefaultSecurityContext(user, "testToken");
            ConfigurationPropertiesFeed configProps = securityContextExecutor.callWithinSecurityContext(
                    configurationPropertiesService::getConfigProps, securityContext);

            // then.
            Map<String, String> sanitizedProperties = configProps.getBeans().stream()
                    .flatMap(bean -> bean.getProperties().stream())
                    .filter(prop -> "******".equals(prop.getValue()))
                    .collect(Collectors.toMap(KeyValue::getKey, KeyValue::getValue));

            assertThat(sanitizedProperties)
                    .containsOnlyKeys(
                            "tags.environment", "tags.version", "tags.forSanitization", "tags.FOR_SANITIZATION")
                    .containsValues("******", "******");

            List<KeyValue> nonSanitizedProps = configProps.getBeans().stream()
                    .flatMap(bean -> bean.getProperties().stream())
                    .filter(prop -> !"******".equals(prop.getValue()))
                    .filter(prop -> prop.getValue() != null)
                    .toList();

            assertThat(nonSanitizedProps)
                    .allMatch(prop -> !prop.getKey().contains("tags.environment")
                            && !prop.getKey().contains("tags.version"));
        }

        @Test
        void shouldReturnUnsanitizedConfigurationProperties_whenUserHasRequiredAuthority() {
            // when.
            User user = createUserWithAuthorities(DefaultAuthority.CONFIG_PROPS_VALUES_READ);
            SecurityContext securityContext = new DefaultSecurityContext(user, "testToken");
            ConfigurationPropertiesFeed configProps = securityContextExecutor.callWithinSecurityContext(
                    configurationPropertiesService::getConfigProps, securityContext);

            // then.
            Set<@Nullable String> values = configProps.getBeans().stream()
                    .flatMap(bean -> bean.getProperties().stream())
                    .map(KeyValue::getValue)
                    .collect(Collectors.toSet());

            assertThat(values).doesNotContain("******");
        }
    }
}
