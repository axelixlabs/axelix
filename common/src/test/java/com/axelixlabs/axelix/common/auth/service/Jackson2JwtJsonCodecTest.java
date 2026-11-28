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
package com.axelixlabs.axelix.common.auth.service;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import com.axelixlabs.axelix.common.contract.jwt.TechUserJwtToken;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for {@link Jackson2JwtJsonCodec}.
 *
 * @author Artemiy Degtyarev
 */
class Jackson2JwtJsonCodecTest {

    private final Jackson2JwtJsonCodec codec = new Jackson2JwtJsonCodec();

    @Test
    void shouldWriteContractModelAsJson() {
        // given.
        TechUserJwtToken token = new TechUserJwtToken("axelix-master", 10L, 70L);

        // when.
        Map<String, Object> written = codec.read(codec.write(token));

        // then.
        assertThat(written).containsOnly(Map.entry("sub", "axelix-master"), Map.entry("iat", 10), Map.entry("exp", 70));
    }

    @Test
    void shouldReadNestedObjectsAsMapsAndArraysAsLists() {
        // given.
        byte[] json = "{\"roles\":[{\"name\":\"ADMIN\",\"authorities\":[\"READ\"]}]}".getBytes(StandardCharsets.UTF_8);

        // when.
        Map<String, Object> result = codec.read(json);

        // then.
        assertThat(result)
                .containsOnly(Map.entry("roles", List.of(Map.of("name", "ADMIN", "authorities", List.of("READ")))));
    }

    @ParameterizedTest
    @ValueSource(strings = {"not json", "[1,2]", "\"string\"", "null", "{\"unterminated\":"})
    void shouldRejectInputThatIsNotJsonObject(String json) {
        // given.
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);

        // when / then.
        assertThatThrownBy(() -> codec.read(bytes)).isInstanceOf(IllegalArgumentException.class);
    }
}
