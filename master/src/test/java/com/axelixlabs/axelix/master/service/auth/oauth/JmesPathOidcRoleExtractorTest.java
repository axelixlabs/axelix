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
    void shouldExtractRoleFromUserInfo() {
        // given.
        String accessToken = "test-access-token";
        String userInfoJson = TestResourceReader.readResource("other/user-info-response.json");

        // and.
        when(oidcClient.validateAccessTokenAndExtractUserInfo(accessToken)).thenReturn(userInfoJson);
        JmesPathOidcRoleExtractor extractor = new JmesPathOidcRoleExtractor(oidcClient, "roles[0]");

        // when.
        Role role = extractor.extractRole(accessToken);

        // then.
        assertThat(role).isEqualTo(DefaultRole.ADMIN);
        verify(oidcClient).validateAccessTokenAndExtractUserInfo(accessToken);
    }

    @MethodSource(value = "absentRoleAttributePath")
    @ParameterizedTest
    void shouldReturnViewerWhenExpressionIsNotSpecified(String roleAttributePath) {
        // given.
        String accessToken = "test-access-token";
        var subject = new JmesPathOidcRoleExtractor(oidcClient, roleAttributePath);

        // when.
        Role role = subject.extractRole(accessToken);

        // then.
        assertThat(role).isEqualTo(DefaultRole.VIEWER);
        verify(oidcClient, never()).validateAccessTokenAndExtractUserInfo(anyString());
    }

    @Test
    void shouldThrowWhenRoleCannotBeExtracted() {
        // given.
        String accessToken = "test-access-token";
        when(oidcClient.validateAccessTokenAndExtractUserInfo(accessToken)).thenReturn("""
            {
                "name" : "Test"
            }
            """);
        JmesPathOidcRoleExtractor extractor = new JmesPathOidcRoleExtractor(oidcClient, "roles[0]");

        // when & then.
        assertThatThrownBy(() -> extractor.extractRole(accessToken)).isInstanceOf(OidcTokenExchangeException.class);
    }

    @Test
    void shouldExtractEditorFromUserInfo() {
        // given.
        String accessToken = "test-access-token";
        when(oidcClient.validateAccessTokenAndExtractUserInfo(accessToken))
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
        var subject = new JmesPathOidcRoleExtractor(oidcClient, "data[0].roles[1]");

        // when.
        Role role = subject.extractRole(accessToken);

        // then.
        assertThat(role).isEqualTo(DefaultRole.EDITOR);
    }

    @Test
    void shouldThrowWhenUserInfoRequestFails() {
        // given.
        String accessToken = "test-access-token";

        when(oidcClient.validateAccessTokenAndExtractUserInfo(accessToken))
                .thenThrow(new OidcTokenExchangeException("userinfo failed"));

        var subject = new JmesPathOidcRoleExtractor(oidcClient, "roles[0]");

        // when & then.
        assertThatThrownBy(() -> subject.extractRole(accessToken)).isInstanceOf(OidcTokenExchangeException.class);
    }

    static Stream<Arguments> absentRoleAttributePath() {
        return Stream.of(of(new Object[] {null}), of(""));
    }
}
