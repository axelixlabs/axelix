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
package com.axelixlabs.axelix.master.api;

import java.util.Objects;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.axelixlabs.axelix.master.ApplicationEntrypoint;
import com.axelixlabs.axelix.master.api.external.endpoint.McpToolApi;
import com.axelixlabs.axelix.master.api.external.response.McpToolFeedResponse;
import com.axelixlabs.axelix.master.utils.InvalidAuthScenario;
import com.axelixlabs.axelix.master.utils.TestRestTemplateBuilder;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link McpToolApi}.
 *
 * @author Sergey Cherkasov
 */
@SpringBootTest(classes = ApplicationEntrypoint.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class McpToolApiTest {

    @Autowired
    private TestRestTemplateBuilder restTemplate;

    @Test
    void should() {
        // when.
        ResponseEntity<McpToolFeedResponse> response = restTemplate
                .withoutAuthorities()
                .getForEntity("/api/external/mcp/tools-feed", McpToolFeedResponse.class);

        // then.
        assertThat(Objects.requireNonNull(response.getBody()).tools())
                .isNotEmpty()
                .hasSizeGreaterThan(0)
                .first()
                .satisfies(tool -> {
                    assertThat(tool.title()).isNotBlank();
                    assertThat(tool.description()).isNotBlank();
                    assertThat(tool.annotations().destructiveHint()).isIn(true, false);
                    assertThat(tool.annotations().idempotentHint()).isIn(true, false);
                    assertThat(tool.annotations().openWorldHint()).isIn(true, false);
                    assertThat(tool.annotations().readOnlyHint()).isIn(true, false);
                    assertThat(tool.status())
                            .isIn(McpToolFeedResponse.ToolStatus.UP, McpToolFeedResponse.ToolStatus.DISABLE);
                });
    }

    @ParameterizedTest
    @EnumSource(InvalidAuthScenario.class)
    void shouldReturnUnauthorized(InvalidAuthScenario scenario) {
        // when.
        ResponseEntity<Void> response =
                scenario.getModifier().apply(restTemplate).getForEntity("/api/external/mcp/tools-feed", Void.class);

        // then.
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }
}
