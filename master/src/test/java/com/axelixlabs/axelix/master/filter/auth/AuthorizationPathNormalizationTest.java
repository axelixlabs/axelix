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
package com.axelixlabs.axelix.master.filter.auth;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests verifying that external API, registration, heartbeat and MCP authentication
 * cannot be bypassed through encoded paths or path parameters.
 *
 * @author Nikita Kirillov
 */
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "axelix.master.mcp-server.enabled=true")
class AuthorizationPathNormalizationTest {

    @LocalServerPort
    private int port;

    static Stream<String> externalPaths() {
        return Stream.of("/api/", "/api;/", "/api;param=value/", "/%61pi/", "/api/%65xternal/../")
                .map(prefix -> prefix + "external/applications/grid");
    }

    @ParameterizedTest
    @MethodSource("externalPaths")
    void rejectsExternalRequestsWithoutCookie(String path) throws Exception {
        // given.
        HttpRequest request = HttpRequest.newBuilder(URI.create("http://localhost:" + port + path))
                .GET()
                .build();

        // when.
        HttpResponse<String> response;
        try (HttpClient client = HttpClient.newHttpClient()) {
            response = client.send(request, HttpResponse.BodyHandlers.ofString());
        }

        // then.
        assertThat(response.statusCode()).isEqualTo(401);
    }

    @ParameterizedTest
    @ValueSource(
            strings = {
                "/api/internal/service/%72egister",
                "/api/internal/service/register;param=value",
                "/api/internal/service/heartbeat/v2;param=value"
            })
    void rejectsInternalRequestsWithoutBearerToken(String path) throws Exception {
        // given.
        HttpRequest request = HttpRequest.newBuilder(URI.create("http://localhost:" + port + path))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString("{}"))
                .build();

        // when.
        HttpResponse<String> response;
        try (HttpClient client = HttpClient.newHttpClient()) {
            response = client.send(request, HttpResponse.BodyHandlers.ofString());
        }

        // then.
        assertThat(response.statusCode()).isEqualTo(401);
    }

    @ParameterizedTest
    @ValueSource(strings = {"/api/mcp", "/api;/mcp", "/%61pi/mcp", "/api/%6dcp", "/api/mcp;param=value"})
    void rejectsMcpRequestsWithoutCredentials(String path) throws Exception {
        // given.
        HttpRequest request = HttpRequest.newBuilder(URI.create("http://localhost:" + port + path))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json, text/event-stream")
                .POST(HttpRequest.BodyPublishers.ofString("""
                        {"jsonrpc":"2.0","id":1,"method":"initialize","params":{
                          "protocolVersion":"2024-11-05","capabilities":{},
                          "clientInfo":{"name":"auth-regression-test","version":"1.0"}}}
                        """))
                .build();

        // when.
        HttpResponse<String> response;
        try (HttpClient client = HttpClient.newHttpClient()) {
            response = client.send(request, HttpResponse.BodyHandlers.ofString());
        }

        // then.
        assertThat(response.statusCode()).isEqualTo(401);
    }
}
