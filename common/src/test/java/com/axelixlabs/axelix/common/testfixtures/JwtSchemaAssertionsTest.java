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
package com.axelixlabs.axelix.common.testfixtures;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static com.axelixlabs.axelix.common.testfixtures.JwtSchemaAssertions.assertJwtPayloadMatchesSchema;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtSchemaAssertionsTest {
    @ParameterizedTest
    @MethodSource("invalidPayloads")
    void shouldRejectPayloadThatViolatesTheWireContract(String schema, String payload) {
        // given.
        String token = "header."
                + Base64.getUrlEncoder().withoutPadding().encodeToString(payload.getBytes(StandardCharsets.UTF_8))
                + ".signature";

        // when / then.
        assertThatThrownBy(() -> assertJwtPayloadMatchesSchema(token, schema)).isInstanceOf(AssertionError.class);
    }

    static Stream<Arguments> invalidPayloads() {
        return Stream.of(
                Arguments.of("TechUserJwtToken", "{\"iat\":1,\"exp\":2}"),
                Arguments.of("TechUserJwtToken", "{\"sub\":\"master\",\"iat\":1,\"exp\":null}"),
                Arguments.of("TechUserJwtToken", "{\"sub\":\"master\",\"iat\":\"1\",\"exp\":2}"),
                Arguments.of("TechUserJwtToken", "{\"sub\":\"master\",\"iat\":1,\"exp\":2,\"extra\":true}"),
                Arguments.of("MasterJwtToken", "{\"sub\":\"user\",\"iat\":1,\"exp\":2,\"roles\":[]}"),
                Arguments.of(
                        "MasterJwtToken",
                        "{\"sub\":\"user\",\"uid\":\"id\",\"iat\":1,\"exp\":2,\"roles\":[{\"name\":\"parent\",\"authorities\":[],\"components\":[{\"name\":\"child\",\"authorities\":[]}]}]}"));
    }
}
