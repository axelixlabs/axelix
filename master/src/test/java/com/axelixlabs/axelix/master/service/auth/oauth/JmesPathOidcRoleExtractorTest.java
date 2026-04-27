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

import java.util.Base64;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import com.axelixlabs.axelix.common.auth.core.DefaultRole;
import com.axelixlabs.axelix.common.auth.core.Role;
import com.axelixlabs.axelix.master.exception.auth.OidcTokenExchangeException;
import com.axelixlabs.axelix.master.utils.TestResourceReader;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.params.provider.Arguments.of;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link JmesPathOidcRoleExtractor}
 *
 * @author Nikita Kirillov
 */
class JmesPathOidcRoleExtractorTest {

    private OidcClient oidcClient;

    @BeforeEach
    void setUp() {
        oidcClient = mock(OidcClient.class);
    }

    @Test
    void shouldExtractRoleFromIdTokenWhenPresent() {
        // given.
        Tokens tokens = tokens("""
        {
            "roles": ["admin", "offline_access"]
        }
        """);
        JmesPathOidcRoleExtractor extractor = new JmesPathOidcRoleExtractor(oidcClient, "roles[0]");

        // when.
        Role role = extractor.extractRole(tokens);

        // then.
        assertThat(role).isEqualTo(DefaultRole.ADMIN);
        verify(oidcClient, never()).validateAccessTokenAndExtractUserInfo(anyString());
    }

    @Test
    void shouldFallbackToUserInfoWhenRoleMissingInIdToken() {
        // given.
        Tokens tokens = tokens("""
            { "sub" : "user123" }
            """);
        String userInfoJson = TestResourceReader.readResource("other/user-info-response.json");
        when(oidcClient.validateAccessTokenAndExtractUserInfo(tokens.accessToken()))
                .thenReturn(userInfoJson);
        JmesPathOidcRoleExtractor extractor = new JmesPathOidcRoleExtractor(oidcClient, "roles[0]");

        // when.
        Role role = extractor.extractRole(tokens);

        // then.
        assertThat(role).isEqualTo(DefaultRole.ADMIN);
        verify(oidcClient).validateAccessTokenAndExtractUserInfo(tokens.accessToken());
    }

    @MethodSource(value = "absentRoleAttributePath")
    @ParameterizedTest
    void shouldReturnViewerWhenExpressionIsNotSpecified(String roleAttributePath) {
        // given.
        Tokens tokens = tokens("""
            { "roles" : ["admin"] }
            """);
        JmesPathOidcRoleExtractor extractor = new JmesPathOidcRoleExtractor(oidcClient, roleAttributePath);

        // when.
        Role role = extractor.extractRole(tokens);

        // then.
        assertThat(role).isEqualTo(DefaultRole.VIEWER);
        verify(oidcClient, never()).validateAccessTokenAndExtractUserInfo(anyString());
    }

    @Test
    void shouldReturnViewerWhenBothSourcesDoNotProvideRole() {
        // given.
        Tokens tokens = tokens("""
            { "sub" : "user123" }
            """);
        when(oidcClient.validateAccessTokenAndExtractUserInfo(tokens.accessToken()))
                .thenReturn("""
            { "name" : "Test" }
            """);
        JmesPathOidcRoleExtractor extractor = new JmesPathOidcRoleExtractor(oidcClient, "roles[0]");

        // when & then.
        assertThatThrownBy(() -> extractor.extractRole(tokens)).isInstanceOf(OidcTokenExchangeException.class);
    }

    @Test
    void shouldExtractEditorFromUserInfo() {
        // given.
        Tokens tokens = tokens("{}");
        when(oidcClient.validateAccessTokenAndExtractUserInfo(tokens.accessToken()))
                .thenReturn(
                        // language=json
                        """
                    {
                        "data": [
                            {
                                "roles": ["viewer", "editor"]
                            }
                        ]
                    }""");
        JmesPathOidcRoleExtractor extractor = new JmesPathOidcRoleExtractor(oidcClient, "data[0].roles[1]");

        // when.
        Role role = extractor.extractRole(tokens);

        // then.
        assertThat(role).isEqualTo(DefaultRole.EDITOR);
    }

    @Test
    void shouldReturnViewerWhenUserInfoRequestFails() {
        // given.
        Tokens tokens = new Tokens("invalid.token.format", "test-access-token");

        when(oidcClient.validateAccessTokenAndExtractUserInfo(tokens.accessToken()))
                .thenThrow(new OidcTokenExchangeException("userinfo failed"));

        JmesPathOidcRoleExtractor extractor = new JmesPathOidcRoleExtractor(oidcClient, "roles[0]");

        // when & then.
        assertThatThrownBy(() -> extractor.extractRole(tokens)).isInstanceOf(OidcTokenExchangeException.class);
    }

    private Tokens tokens(String idTokenPayloadJson) {
        // header and signature are also supposed to be base64 encoded, but we're not really interested in them in this
        // test class
        String idToken =
                "header." + Base64.getUrlEncoder().encodeToString(idTokenPayloadJson.getBytes()) + ".signature";
        return new Tokens(idToken, "test-access-token");
    }

    static Stream<Arguments> absentRoleAttributePath() {
        return Stream.of(of(new Object[] {null}), of(""));
    }
}
