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
package com.axelixlabs.axelix.master.mcp.auth;

import java.util.Optional;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import tools.jackson.databind.json.JsonMapper;

import com.axelixlabs.axelix.master.exception.auth.AuthenticationException;
import com.axelixlabs.axelix.master.mcp.McpEndpoint;
import com.axelixlabs.axelix.master.mcp.McpEndpoints;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for {@link DefaultMcpEndpointResolver}.
 *
 * @author Mikhail Polivakha
 */
class DefaultMcpEndpointResolverTest {

    private final DefaultMcpEndpointResolver subject = new DefaultMcpEndpointResolver(new JsonMapper());

    @ParameterizedTest
    @MethodSource(value = "endpointMapping")
    void shouldResolveEndpointForToolsCallRequest(String toolName, McpEndpoint expectedEndpoint) {
        // given.
        String request = """
                {
                  "jsonrpc": "2.0",
                  "id": 1,
                  "method": "tools/call",
                  "params": {
                    "name": "%s",
                    "arguments": {}
                  }
                }
                """.formatted(toolName);

        // when.
        Optional<McpEndpoint> result = subject.resolve(request);

        // then.
        assertThat(result).contains(expectedEndpoint);
    }

    @Test
    void shouldReturnEmptyWhenMethodIsNotToolCallAndNotEndpointName() {
        // given.
        String request = """
                {
                  "jsonrpc": "2.0",
                  "id": 1,
                  "method": "tools/list"
                }
                """;

        // when.
        Optional<McpEndpoint> result = subject.resolve(request);

        // then.
        assertThat(result).isEmpty();
    }

    @Test
    void shouldThrowAuthenticationExceptionWhenRequestBodyIsInvalidJson() {
        // given.
        String request = "{ invalid";

        // when.
        // then.
        assertThatThrownBy(() -> subject.resolve(request)).isInstanceOf(AuthenticationException.class);
    }

    @Test
    void shouldThrowAuthenticationExceptionWhenMethodIsMissing() {
        // given.
        String request = """
                {
                  "jsonrpc": "2.0",
                  "id": 1
                }
                """;

        // when.
        // then.
        assertThatThrownBy(() -> subject.resolve(request)).isInstanceOf(AuthenticationException.class);
    }

    @Test
    void shouldThrowAuthenticationExceptionWhenToolCallHasNoToolName() {
        // given.
        String request = """
                {
                  "jsonrpc": "2.0",
                  "id": 1,
                  "method": "tools/call",
                  "params": {
                    "arguments": {}
                  }
                }
                """;

        // when.
        // then.
        assertThatThrownBy(() -> subject.resolve(request)).isInstanceOf(AuthenticationException.class);
    }

    public static Stream<Arguments> endpointMapping() {
        return Stream.of(
                Arguments.of(McpEndpoints.BEANS_FEED_TOOL_NAME, McpEndpoints.BEANS_FEED),
                Arguments.of(McpEndpoints.ENVIRONMENT_FEED_TOOL_NAME, McpEndpoints.ENVIRONMENT),
                Arguments.of(McpEndpoints.CONFIG_PROPS_FEED_TOOL_NAME, McpEndpoints.CONFIG_PROPS),
                Arguments.of(McpEndpoints.CONDITIONS_FEED_TOOL_NAME, McpEndpoints.CONDITIONS),
                Arguments.of(McpEndpoints.SCHEDULED_TASKS_FEED_TOOL_NAME, McpEndpoints.SCHEDULED_TASKS),
                Arguments.of(McpEndpoints.WALLBOARD_TOOL_NAME, McpEndpoints.WALLBOARD),
                Arguments.of(McpEndpoints.CACHES_FEED_TOOL_NAME, McpEndpoints.ALL_CACHES),
                Arguments.of(McpEndpoints.CLEAR_ALL_CACHES_TOOL_NAME, McpEndpoints.CLEAR_ALL_CACHES),
                Arguments.of(McpEndpoints.CLEAR_SPECIFIC_CACHE_TOOL_NAME, McpEndpoints.CLEAR_SPECIFIC_CACHE));
    }
}
