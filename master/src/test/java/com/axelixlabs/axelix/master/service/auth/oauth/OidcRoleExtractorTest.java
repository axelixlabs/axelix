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
package com.axelixlabs.axelix.master.service.auth.oauth;

import java.lang.reflect.Method;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import com.axelixlabs.axelix.common.auth.core.DefaultRole;
import com.axelixlabs.axelix.common.auth.core.Role;
import com.axelixlabs.axelix.master.exception.auth.OidcTokenExchangeException;
import com.axelixlabs.axelix.master.service.auth.oauth.DefaultOidcClient.Tokens;
import com.axelixlabs.axelix.master.utils.TestResourceReader;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link OidcRoleExtractor}
 *
 * @author Nikita Kirillov
 */
class OidcRoleExtractorTest {

    private OidcClient MOCK_OIDC_CLIENT;

    @BeforeEach
    void setUp() {
        MOCK_OIDC_CLIENT = mock(OidcClient.class);
    }

    @Nested
    class ExtractRoleIntegrationTest {

        private static final String ID_TOKEN =
                "header." + Base64.getUrlEncoder().encodeToString("""
        {
            "roles": ["admin", "offline_access"]
        }
        """.getBytes()) + ".signature";

        private static final String ACCESS_TOKEN = "test-access-token";
        private static final String USER_INFO_JSON = TestResourceReader.readResource("other/user-info-response.json");

        private OidcRoleExtractor extractor;

        @Test
        void shouldExtractRoleFromIdTokenWhenPresent() {
            Tokens tokens = new Tokens(ID_TOKEN, ACCESS_TOKEN);
            extractor = new OidcRoleExtractor(MOCK_OIDC_CLIENT, "$.roles", Map.of());

            Role role = extractor.extractRole(tokens);

            assertThat(role).isEqualTo(DefaultRole.ADMIN);
            verify(MOCK_OIDC_CLIENT, never()).validateAccessTokenAndExtractUserInfo(anyString());
        }

        @Test
        void shouldFallbackToUserInfoWhenIdTokenHasNoRole() {
            String idTokenWithoutRoles = "header."
                    + Base64.getUrlEncoder().encodeToString("{\"sub\": \"user123\"}".getBytes()) + ".signature";
            Tokens tokens = new Tokens(idTokenWithoutRoles, ACCESS_TOKEN);

            when(MOCK_OIDC_CLIENT.validateAccessTokenAndExtractUserInfo(ACCESS_TOKEN))
                    .thenReturn(USER_INFO_JSON);

            extractor = new OidcRoleExtractor(MOCK_OIDC_CLIENT, "$.roles", Map.of());

            Role role = extractor.extractRole(tokens);

            assertThat(role).isEqualTo(DefaultRole.ADMIN);
            verify(MOCK_OIDC_CLIENT).validateAccessTokenAndExtractUserInfo(ACCESS_TOKEN);
        }

        @Test
        void shouldFallbackToUserInfoWhenIdTokenExtractionFails() {
            String invalidIdToken = "invalid.token.format";
            Tokens tokens = new Tokens(invalidIdToken, ACCESS_TOKEN);

            when(MOCK_OIDC_CLIENT.validateAccessTokenAndExtractUserInfo(ACCESS_TOKEN))
                    .thenReturn(USER_INFO_JSON);

            extractor = new OidcRoleExtractor(MOCK_OIDC_CLIENT, "$.roles", Map.of());

            Role role = extractor.extractRole(tokens);

            assertThat(role).isEqualTo(DefaultRole.ADMIN);
            verify(MOCK_OIDC_CLIENT).validateAccessTokenAndExtractUserInfo(ACCESS_TOKEN);
        }

        @Test
        void shouldReturnViewerWhenBothSourcesFail() {
            String invalidIdToken = "invalid.token.format";
            Tokens tokens = new Tokens(invalidIdToken, ACCESS_TOKEN);

            when(MOCK_OIDC_CLIENT.validateAccessTokenAndExtractUserInfo(ACCESS_TOKEN))
                    .thenThrow(new OidcTokenExchangeException("UserInfo endpoint failed"));

            extractor = new OidcRoleExtractor(MOCK_OIDC_CLIENT, "$.roles", Map.of());

            Role role = extractor.extractRole(tokens);

            assertThat(role).isEqualTo(DefaultRole.VIEWER);
        }

        @Test
        void shouldReturnViewerWhenNoRoleFoundInBothSources() {
            String idTokenWithoutRoles = "header."
                    + Base64.getUrlEncoder().encodeToString("{\"sub\": \"user123\"}".getBytes()) + ".signature";
            String userInfoWithoutRoles = """
            {
                "sub": "user123",
                "name": "Test User"
            }
            """;

            Tokens tokens = new Tokens(idTokenWithoutRoles, ACCESS_TOKEN);

            when(MOCK_OIDC_CLIENT.validateAccessTokenAndExtractUserInfo(ACCESS_TOKEN))
                    .thenReturn(userInfoWithoutRoles);

            extractor = new OidcRoleExtractor(MOCK_OIDC_CLIENT, "$.roles", Map.of());

            Role role = extractor.extractRole(tokens);

            assertThat(role).isEqualTo(DefaultRole.VIEWER);
        }

        @Test
        void shouldApplyRoleMappingFromIdToken() {
            Tokens tokens = new Tokens(ID_TOKEN, ACCESS_TOKEN);
            Map<String, List<String>> roleMapping = Map.of(
                    "admin", List.of("super_admin", "root"),
                    "editor", List.of("writer"));

            extractor = new OidcRoleExtractor(MOCK_OIDC_CLIENT, "$.roles", roleMapping);

            Role role = extractor.extractRole(tokens);

            assertThat(role).isEqualTo(DefaultRole.ADMIN);
        }

        @Test
        void shouldApplyRoleMappingFromUserInfoWhenIdTokenHasNoRole() {
            String idTokenWithoutRoles = "header."
                    + Base64.getUrlEncoder().encodeToString("{\"sub\": \"user123\"}".getBytes()) + ".signature";
            String userInfoWithMappedRole = """
            {
                "role": "super_admin"
            }
            """;

            Tokens tokens = new Tokens(idTokenWithoutRoles, ACCESS_TOKEN);

            when(MOCK_OIDC_CLIENT.validateAccessTokenAndExtractUserInfo(ACCESS_TOKEN))
                    .thenReturn(userInfoWithMappedRole);

            Map<String, List<String>> roleMapping = Map.of("admin", List.of("super_admin", "root"));
            extractor = new OidcRoleExtractor(MOCK_OIDC_CLIENT, "$.role", roleMapping);

            Role role = extractor.extractRole(tokens);

            assertThat(role).isEqualTo(DefaultRole.ADMIN);
        }

        @Test
        void shouldExtractEditorRoleFromUserInfo() {
            String idTokenWithoutRoles =
                    "header." + Base64.getUrlEncoder().encodeToString("{}".getBytes()) + ".signature";
            String userInfoWithEditor = """
            {
                "roles": ["editor", "viewer"]
            }
            """;

            Tokens tokens = new Tokens(idTokenWithoutRoles, ACCESS_TOKEN);

            when(MOCK_OIDC_CLIENT.validateAccessTokenAndExtractUserInfo(ACCESS_TOKEN))
                    .thenReturn(userInfoWithEditor);

            extractor = new OidcRoleExtractor(MOCK_OIDC_CLIENT, "$.roles", Map.of());

            Role role = extractor.extractRole(tokens);

            assertThat(role).isEqualTo(DefaultRole.EDITOR);
        }

        @Test
        void shouldReturnViewerWhenRoleAttributePathIsBlank() {
            Tokens tokens = new Tokens(ID_TOKEN, ACCESS_TOKEN);

            extractor = new OidcRoleExtractor(MOCK_OIDC_CLIENT, "", Map.of());

            Role role = extractor.extractRole(tokens);

            assertThat(role).isEqualTo(DefaultRole.VIEWER);
            verify(MOCK_OIDC_CLIENT, never()).validateAccessTokenAndExtractUserInfo(anyString());
        }

        @Test
        void shouldReturnViewerWhenRoleAttributePathIsNull() {
            Tokens tokens = new Tokens(ID_TOKEN, ACCESS_TOKEN);

            extractor = new OidcRoleExtractor(MOCK_OIDC_CLIENT, null, Map.of());

            Role role = extractor.extractRole(tokens);

            assertThat(role).isEqualTo(DefaultRole.VIEWER);
            verify(MOCK_OIDC_CLIENT, never()).validateAccessTokenAndExtractUserInfo(anyString());
        }

        @Test
        void shouldExtractFromUserInfoWhenIdTokenHasNullRole() {
            String idTokenWithNullRole =
                    "header." + Base64.getUrlEncoder().encodeToString("{\"roles\": null}".getBytes()) + ".signature";
            Tokens tokens = new Tokens(idTokenWithNullRole, ACCESS_TOKEN);

            when(MOCK_OIDC_CLIENT.validateAccessTokenAndExtractUserInfo(ACCESS_TOKEN))
                    .thenReturn(USER_INFO_JSON);

            extractor = new OidcRoleExtractor(MOCK_OIDC_CLIENT, "$.roles", Map.of());

            Role role = extractor.extractRole(tokens);

            assertThat(role).isEqualTo(DefaultRole.ADMIN);
            verify(MOCK_OIDC_CLIENT).validateAccessTokenAndExtractUserInfo(ACCESS_TOKEN);
        }

        @Test
        void shouldUseFirstMatchingRoleFromArray() {
            String idTokenWithMultipleRoles = "header."
                    + Base64.getUrlEncoder()
                            .encodeToString("{\"roles\": [\"viewer\", \"editor\", \"admin\"]}".getBytes())
                    + ".signature";
            Tokens tokens = new Tokens(idTokenWithMultipleRoles, ACCESS_TOKEN);

            extractor = new OidcRoleExtractor(MOCK_OIDC_CLIENT, "$.roles[0]", Map.of());

            Role role = extractor.extractRole(tokens);

            assertThat(role).isEqualTo(DefaultRole.VIEWER);
        }
    }

    /**
     * Unit tests for {@link OidcRoleExtractor#extractRoleFromJson(String json)}
     *
     * @author Nikita Kirillov
     */
    @Nested
    class ExtractRoleFromJsonPrivateMethodTest {

        private Method extractRoleFromJsonMethod;
        private OidcRoleExtractor extractor;

        @BeforeEach
        void setUp() throws NoSuchMethodException {
            extractRoleFromJsonMethod = OidcRoleExtractor.class.getDeclaredMethod("extractRoleFromJson", String.class);
            extractRoleFromJsonMethod.setAccessible(true);
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"", "   ", "\t", "\n", "  \t  "})
        void shouldReturnNullWhenRoleAttributePathIsBlank(String path) throws Exception {
            extractor = new OidcRoleExtractor(MOCK_OIDC_CLIENT, path, Map.of());

            Role role = (Role) extractRoleFromJsonMethod.invoke(extractor, "{\"role\": \"admin\"}");

            assertThat(role).isNull();
        }

        @ParameterizedTest
        @MethodSource("provideJsonAndExpectedRole")
        void shouldExtractCorrectRoleFromJson(
                String json, String roleAttributePath, Map<String, List<String>> roleMapping, DefaultRole expectedRole)
                throws Exception {
            extractor = new OidcRoleExtractor(MOCK_OIDC_CLIENT, roleAttributePath, roleMapping);

            Role role = (Role) extractRoleFromJsonMethod.invoke(extractor, json);

            assertThat(role).as("JSON: %s, Path: %s", json, roleAttributePath).isEqualTo(expectedRole);
        }

        private static Stream<Arguments> provideJsonAndExpectedRole() {
            return Stream.of(
                    // Basic role extraction
                    Arguments.of("{\"role\": \"admin\"}", "$.role", Map.of(), DefaultRole.ADMIN),
                    Arguments.of("{\"role\": \"editor\"}", "$.role", Map.of(), DefaultRole.EDITOR),
                    Arguments.of("{\"role\": \"viewer\"}", "$.role", Map.of(), null),
                    Arguments.of("{\"role\": \"unknown\"}", "$.role", Map.of(), null),

                    // Nested paths
                    Arguments.of("{\"user\": {\"access\": \"admin\"}}", "$.user.access", Map.of(), DefaultRole.ADMIN),
                    Arguments.of(
                            "{\"data\": {\"info\": {\"level\": \"editor\"}}}",
                            "$.data.info.level",
                            Map.of(),
                            DefaultRole.EDITOR),

                    // Array handling
                    Arguments.of("{\"roles\": [\"admin\", \"user\"]}", "$.roles", Map.of(), DefaultRole.ADMIN),
                    Arguments.of("{\"roles\": [\"viewer\", \"editor\"]}", "$.roles", Map.of(), DefaultRole.EDITOR),
                    Arguments.of("{\"roles\": [\"guest\", \"user\"]}", "$.roles", Map.of(), null),
                    Arguments.of("{\"roles\": [\"editor\"]}", "$.roles", Map.of(), DefaultRole.EDITOR),

                    // Case insensitivity
                    Arguments.of("{\"role\": \"ADMIN\"}", "$.role", Map.of(), DefaultRole.ADMIN),
                    Arguments.of("{\"role\": \"Admin\"}", "$.role", Map.of(), DefaultRole.ADMIN),
                    Arguments.of("{\"role\": \"EDITOR\"}", "$.role", Map.of(), DefaultRole.EDITOR),
                    Arguments.of("{\"role\": \"EdItOr\"}", "$.role", Map.of(), DefaultRole.EDITOR),
                    Arguments.of("{\"role\": \"ViEwEr\"}", "$.role", Map.of(), null),

                    // Role mapping
                    Arguments.of(
                            "{\"role\": \"superuser\"}",
                            "$.role",
                            Map.of("admin", List.of("superuser", "root")),
                            DefaultRole.ADMIN),
                    Arguments.of(
                            "{\"role\": \"power_user\"}",
                            "$.role",
                            Map.of("editor", List.of("power_user", "content_creator")),
                            DefaultRole.EDITOR),
                    Arguments.of(
                            "{\"role\": \"SUPERUSER\"}",
                            "$.role",
                            Map.of("admin", List.of("superuser", "root")),
                            DefaultRole.ADMIN),
                    Arguments.of("{\"role\": \"unknown\"}", "$.role", Map.of("admin", List.of("superuser")), null),
                    Arguments.of(
                            "{\"role\": \"editor\"}",
                            "$.role",
                            Map.of("admin", List.of("superuser")),
                            DefaultRole.EDITOR));
        }

        @ParameterizedTest
        @MethodSource("provideRoleMappingWithMultipleValues")
        void shouldApplyRoleMappingWithMultipleValues(
                String json, Map<String, List<String>> roleMapping, DefaultRole expectedRole) throws Exception {
            extractor = new OidcRoleExtractor(MOCK_OIDC_CLIENT, "$.role", roleMapping);

            Role role = (Role) extractRoleFromJsonMethod.invoke(extractor, json);

            assertThat(role).isEqualTo(expectedRole);
        }

        private static Stream<Arguments> provideRoleMappingWithMultipleValues() {
            Map<String, List<String>> roleMapping = Map.of(
                    "admin", List.of("super_admin", "root", "sysadmin"),
                    "editor", List.of("content_editor", "blog_writer"));

            return Stream.of(
                    Arguments.of("{\"role\": \"super_admin\"}", roleMapping, DefaultRole.ADMIN),
                    Arguments.of("{\"role\": \"root\"}", roleMapping, DefaultRole.ADMIN),
                    Arguments.of("{\"role\": \"sysadmin\"}", roleMapping, DefaultRole.ADMIN),
                    Arguments.of("{\"role\": \"content_editor\"}", roleMapping, DefaultRole.EDITOR),
                    Arguments.of("{\"role\": \"blog_writer\"}", roleMapping, DefaultRole.EDITOR),
                    Arguments.of("{\"role\": \"unknown\"}", roleMapping, null));
        }

        @ParameterizedTest
        @MethodSource("provideEdgeCaseJson")
        void shouldHandleEdgeCasesGracefully(String json, String path, DefaultRole expectedRole) throws Exception {
            extractor = new OidcRoleExtractor(MOCK_OIDC_CLIENT, path, Map.of());

            Role role = (Role) extractRoleFromJsonMethod.invoke(extractor, json);

            assertThat(role).as("JSON: %s, Path: %s", json, path).isEqualTo(expectedRole);
        }

        private static Stream<Arguments> provideEdgeCaseJson() {
            return Stream.of(
                    Arguments.of("{}", "$.role", null),
                    Arguments.of("{\"role\": null}", "$.role", null),
                    Arguments.of("{\"role\": 1}", "$.role", null),
                    Arguments.of("{\"role\": 123}", "$.role", null),
                    Arguments.of("{\"role\": 0}", "$.role", null),
                    Arguments.of("{\"role\": true}", "$.role", null),
                    Arguments.of("{\"role\": false}", "$.role", null),
                    Arguments.of("{\"roles\": []}", "$.roles", null),
                    Arguments.of("{invalid json}", "$.role", null),
                    Arguments.of("plain string", "$.role", null),
                    Arguments.of("12345", "$.role", null),
                    Arguments.of("{\"user\": \"admin\"}", "$.role", null),
                    Arguments.of("{\"data\": {\"value\": \"admin\"}}", "$.role", null));
        }

        @Test
        void shouldHandleDeeplyNestedArrays() throws Exception {
            extractor = new OidcRoleExtractor(MOCK_OIDC_CLIENT, "$.data[0].users[1].roles[0]", Map.of());

            String json = """
                {
                    "data": [
                        {
                            "users": [
                                {"name": "user1", "roles": ["viewer"]},
                                {"name": "user2", "roles": ["admin", "editor"]}
                            ]
                        }
                    ]
                }
                """;

            Role role = (Role) extractRoleFromJsonMethod.invoke(extractor, json);
            assertThat(role).isEqualTo(DefaultRole.ADMIN);
        }

        @Test
        void shouldHandleCaseInsensitiveInRoleMapping() throws Exception {
            Map<String, List<String>> roleMapping = Map.of(
                    "admin", List.of("SuperAdmin", "ROOT"),
                    "editor", List.of("ContentEditor"));
            extractor = new OidcRoleExtractor(MOCK_OIDC_CLIENT, "$.role", roleMapping);

            assertThat((Role) extractRoleFromJsonMethod.invoke(extractor, "{\"role\": \"superadmin\"}"))
                    .isEqualTo(DefaultRole.ADMIN);
            assertThat((Role) extractRoleFromJsonMethod.invoke(extractor, "{\"role\": \"root\"}"))
                    .isEqualTo(DefaultRole.ADMIN);
            assertThat((Role) extractRoleFromJsonMethod.invoke(extractor, "{\"role\": \"ROOT\"}"))
                    .isEqualTo(DefaultRole.ADMIN);
            assertThat((Role) extractRoleFromJsonMethod.invoke(extractor, "{\"role\": \"contenteditor\"}"))
                    .isEqualTo(DefaultRole.EDITOR);
            assertThat((Role) extractRoleFromJsonMethod.invoke(extractor, "{\"role\": \"ContentEditor\"}"))
                    .isEqualTo(DefaultRole.EDITOR);
        }

        @ParameterizedTest
        @MethodSource("provideDifferentPathFormats")
        void shouldHandleDifferentJsonPathFormats(String path, String json, DefaultRole expectedRole) throws Exception {
            extractor = new OidcRoleExtractor(MOCK_OIDC_CLIENT, path, Map.of());

            Role role = (Role) extractRoleFromJsonMethod.invoke(extractor, json);

            assertThat(role).as("Path: %s should extract correct role", path).isEqualTo(expectedRole);
        }

        private static Stream<Arguments> provideDifferentPathFormats() {
            return Stream.of(
                    Arguments.of("$.data.role", "{\"data\": {\"role\": \"admin\"}}", DefaultRole.ADMIN),
                    Arguments.of("$.roles[0]", "{\"roles\": [\"admin\", \"editor\"]}", DefaultRole.ADMIN),
                    Arguments.of("$.roles[1]", "{\"roles\": [\"admin\", \"editor\"]}", DefaultRole.EDITOR),
                    Arguments.of("$.roles[*]", "{\"roles\": [\"editor\", \"admin\"]}", DefaultRole.EDITOR),
                    Arguments.of(
                            "$.user.info.role", "{\"user\": {\"info\": {\"role\": \"editor\"}}}", DefaultRole.EDITOR),
                    Arguments.of(
                            "$.data[0].roles[1]",
                            "{\"data\": [{\"roles\": [\"viewer\", \"admin\"]}]}",
                            DefaultRole.ADMIN));
        }

        @Test
        void shouldReturnNullWhenPathDoesNotExist() throws Exception {
            extractor = new OidcRoleExtractor(MOCK_OIDC_CLIENT, "$.nonexistent", Map.of());

            Role role = (Role) extractRoleFromJsonMethod.invoke(extractor, "{\"role\": \"admin\"}");

            assertThat(role).isNull();
        }

        @Test
        void shouldReturnNullWhenJsonIsNull() throws Exception {
            extractor = new OidcRoleExtractor(MOCK_OIDC_CLIENT, "$.role", Map.of());

            Role role = (Role) extractRoleFromJsonMethod.invoke(extractor, (Object) null);

            assertThat(role).isNull();
        }
    }
}
