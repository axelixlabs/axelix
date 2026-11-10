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
package com.axelixlabs.axelix.master.service.transport;

import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import tools.jackson.databind.ObjectMapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.axelixlabs.axelix.common.contract.configprops.ConfigurationPropertiesEntry;
import com.axelixlabs.axelix.common.contract.configprops.ConfigurationPropertiesFeed;
import com.axelixlabs.axelix.common.contract.env.EnvironmentFeed;
import com.axelixlabs.axelix.common.contract.env.Property;
import com.axelixlabs.axelix.master.domain.auth.DefaultRole;
import com.axelixlabs.axelix.master.domain.auth.DefaultSecurityContext;
import com.axelixlabs.axelix.master.domain.auth.PasswordlessUser;
import com.axelixlabs.axelix.master.domain.auth.Role;
import com.axelixlabs.axelix.master.domain.auth.SecurityContextExecutor;
import com.axelixlabs.axelix.master.service.auth.AuthoritiesService;
import com.axelixlabs.axelix.master.service.auth.jwt.UserJwtEncoderService;
import com.axelixlabs.axelix.master.service.state.auth.RoleService;

import static com.axelixlabs.axelix.master.service.transport.PropertySanitizer.SANITIZED_VALUE;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link PropertySanitizer}.
 *
 * @author Nikita Kirillov
 */
@SpringBootTest
class PropertySanitizerTest {

    // language=json
    private static final String ENVIRONMENT_JSON = """
            {
              "propertySources": [
                {
                  "properties": [
                    {
                      "value": "secret",
                      "sensitive": true
                    },
                    {
                      "value": "public",
                      "sensitive": false
                    },
                    {
                      "value": "legacy"
                    },
                    {
                      "value": null,
                      "sensitive": true
                    }
                  ]
                }
              ]
            }
            """;

    // language=json
    private static final String CONFIG_PROPS_JSON = """
            {
              "beans": [
                {
                  "properties": [
                    {
                      "value": "secret",
                      "sensitive": true
                    },
                    {
                      "value": "public",
                      "sensitive": false
                    },
                    {
                      "value": "legacy"
                    },
                    {
                      "value": null,
                      "sensitive": true
                    }
                  ],
                  "inputs": [
                    {
                      "value": "secret",
                      "sensitive": true
                    },
                    {
                      "value": "public",
                      "sensitive": false
                    },
                    {
                      "value": "legacy"
                    },
                    {
                      "value": null,
                      "sensitive": true
                    }
                  ]
                }
              ]
            }
            """;

    @Autowired
    private PropertySanitizer sanitizer;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private SecurityContextExecutor securityContextExecutor;

    @Autowired
    private UserJwtEncoderService tokenEncoder;

    @Autowired
    private RoleService roleService;

    @Autowired
    private AuthoritiesService authoritiesService;

    @Nested
    class EnvironmentSanitization {
        private EnvironmentFeed feed;

        @BeforeEach
        void setUp() {
            feed = objectMapper.readValue(ENVIRONMENT_JSON, EnvironmentFeed.class);
        }

        @ParameterizedTest
        @CsvSource({"VIEWER," + SANITIZED_VALUE, "ADMIN,secret"})
        void appliesUserPermissions(String roleName, String expectedSecret) {
            // given.
            Role role = roleService.findByName(roleName).orElseThrow();

            // when.
            runAs(role, () -> sanitizer.sanitize(feed));

            // then.
            assertThat(feed.getPropertySources().get(0).getProperties())
                    .extracting(Property::getValue)
                    .containsExactly(expectedSecret, "public", "legacy", null);
        }

        @Test
        void masksWithoutSecurityContext() {
            // when.
            sanitizer.sanitize(feed);

            // then.
            assertThat(feed.getPropertySources().get(0).getProperties())
                    .extracting(Property::getValue)
                    .containsExactly(SANITIZED_VALUE, "public", "legacy", null);
        }

        @Test
        void configurationPropertiesPermissionDoesNotRevealEnvironmentValues() {
            // given.
            Role role = roleWithPermission("config-properties.values:read");

            // when.
            runAs(role, () -> sanitizer.sanitize(feed));

            // then.
            assertThat(feed.getPropertySources().get(0).getProperties())
                    .extracting(Property::getValue)
                    .containsExactly(SANITIZED_VALUE, "public", "legacy", null);
        }
    }

    @Nested
    class ConfigurationPropertiesSanitization {
        private ConfigurationPropertiesFeed feed;

        @BeforeEach
        void setUp() {
            feed = objectMapper.readValue(CONFIG_PROPS_JSON, ConfigurationPropertiesFeed.class);
        }

        @ParameterizedTest
        @CsvSource({"VIEWER," + SANITIZED_VALUE, "ADMIN,secret"})
        void appliesUserPermissionsToPropertiesAndInputs(String roleName, String expectedSecret) {
            // given.
            Role role = roleService.findByName(roleName).orElseThrow();

            // when.
            runAs(role, () -> sanitizer.sanitize(feed));

            // then.
            assertThat(feed.getBeans().get(0).getProperties())
                    .extracting(ConfigurationPropertiesEntry::getValue)
                    .containsExactly(expectedSecret, "public", "legacy", null);
            assertThat(feed.getBeans().get(0).getInputs())
                    .extracting(ConfigurationPropertiesEntry::getValue)
                    .containsExactly(expectedSecret, "public", "legacy", null);
        }

        @Test
        void masksWithoutSecurityContext() {
            // when.
            sanitizer.sanitize(feed);

            // then.
            assertThat(feed.getBeans().get(0).getProperties())
                    .extracting(ConfigurationPropertiesEntry::getValue)
                    .containsExactly(SANITIZED_VALUE, "public", "legacy", null);
            assertThat(feed.getBeans().get(0).getInputs())
                    .extracting(ConfigurationPropertiesEntry::getValue)
                    .containsExactly(SANITIZED_VALUE, "public", "legacy", null);
        }

        @Test
        void environmentPermissionDoesNotRevealConfigurationValues() {
            // given.
            Role role = roleWithPermission("environment.values:read");

            // when.
            runAs(role, () -> sanitizer.sanitize(feed));

            // then.
            assertThat(feed.getBeans().get(0).getProperties())
                    .extracting(ConfigurationPropertiesEntry::getValue)
                    .containsExactly(SANITIZED_VALUE, "public", "legacy", null);
            assertThat(feed.getBeans().get(0).getInputs())
                    .extracting(ConfigurationPropertiesEntry::getValue)
                    .containsExactly(SANITIZED_VALUE, "public", "legacy", null);
        }
    }

    private Role roleWithPermission(String codename) {
        return new DefaultRole(
                "test",
                Set.of(authoritiesService.getAll().stream()
                        .filter(authority -> authority.getName().equals(codename))
                        .findFirst()
                        .orElseThrow()));
    }

    private void runAs(Role role, Runnable action) {
        Role inheritedRole = new DefaultRole("inherited", Set.of(), Set.of(role));
        String token =
                tokenEncoder.generateToken(new PasswordlessUser("test-user", "test-user", Set.of(inheritedRole)));
        securityContextExecutor.runWithinSecurityContext(action::run, new DefaultSecurityContext(token));
    }
}
