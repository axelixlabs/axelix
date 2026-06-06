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

import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import com.axelixlabs.axelix.common.auth.core.DefaultRole;
import com.axelixlabs.axelix.common.auth.core.Role;
import com.axelixlabs.axelix.master.exception.auth.OidcRoleExtractionException;
import com.axelixlabs.axelix.master.utils.TestResourceReader;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.params.provider.Arguments.of;

/**
 * Tests for {@link JmesPathOidcRoleExtractor}
 *
 * @author Nikita Kirillov
 */
class JmesPathOidcRoleExtractorTest {

    @Test
    void shouldExtractRoleFromUserInfo() {
        // given.
        String userInfoJson = TestResourceReader.readResource("other/user-info-response.json");

        // and.
        JmesPathOidcRoleExtractor extractor = new JmesPathOidcRoleExtractor("roles[0]");

        // when.
        Role role = extractor.extractRole(userInfoJson);

        // then.
        assertThat(role).isEqualTo(DefaultRole.ADMIN);
    }

    @MethodSource(value = "absentRoleAttributePath")
    @ParameterizedTest
    void shouldReturnViewerWhenExpressionIsNotSpecified(String roleAttributePath) {
        // given.
        String userInfoJson = "someJson";
        var subject = new JmesPathOidcRoleExtractor(roleAttributePath);

        // when.
        Role role = subject.extractRole(userInfoJson);

        // then.
        assertThat(role).isEqualTo(DefaultRole.VIEWER);
    }

    @Test
    void shouldThrowWhenRoleCannotBeExtracted() {
        // given.
        String userInfoJson = "someJson";

        JmesPathOidcRoleExtractor extractor = new JmesPathOidcRoleExtractor("roles[0]");

        // when & then.
        assertThatThrownBy(() -> extractor.extractRole(userInfoJson)).isInstanceOf(OidcRoleExtractionException.class);
    }

    @Test
    void shouldExtractEditorFromUserInfo() {
        // given.
        // language=json
        String userInfoJson = """
                    {
                        "data": [
                            {
                                "roles": ["viewer", "editor"]
                            }
                        ]
                    }""";
        var subject = new JmesPathOidcRoleExtractor("data[0].roles[1]");

        // when.
        Role role = subject.extractRole(userInfoJson);

        // then.
        assertThat(role).isEqualTo(DefaultRole.EDITOR);
    }

    static Stream<Arguments> absentRoleAttributePath() {
        return Stream.of(of(new Object[] {null}), of(""));
    }
}
