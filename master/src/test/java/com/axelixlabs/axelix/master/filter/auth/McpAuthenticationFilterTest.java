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

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.restclient.RestTemplateBuilder;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

import com.axelixlabs.axelix.master.ApplicationEntrypoint;
import com.axelixlabs.axelix.master.autoconfiguration.McpAutoConfiguration;
import com.axelixlabs.axelix.master.service.state.InstanceRegistry;
import com.axelixlabs.axelix.master.utils.TestObjectFactory;

import static com.axelixlabs.axelix.master.utils.ContentType.ACTUATOR_RESPONSE_CONTENT_TYPE;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link McpAuthenticationFilter}.
 *
 * @author Mikhail Polivakha
 */
@SpringBootTest(classes = ApplicationEntrypoint.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(McpAutoConfiguration.class)
@TestPropertySource(
        properties = {
            "axelix.master.auth.options.static-admin.enabled=true",
            "axelix.master.auth.options.static-admin.credentials.username=admin",
            "axelix.master.auth.options.static-admin.credentials.password=admin"
        })
class McpAuthenticationFilterTest {

    private static final String MCP_PROTOCOL_VERSION_HEADER = "MCP-Protocol-Version";
    private static final String MCP_PROTOCOL_VERSION = "2024-11-05";

    private static MockWebServer mockWebServer;

    @Autowired
    private InstanceRegistry instanceRegistry;

    @LocalServerPort
    private int port;

    private TestRestTemplate restTemplate;

    @BeforeAll
    static void startMockWebServer() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
    }

    @AfterAll
    static void stopMockWebServer() throws IOException {
        mockWebServer.shutdown();
    }

    @BeforeEach
    void setUpRestTemplate() {
        this.restTemplate = new TestRestTemplate(
                new RestTemplateBuilder().rootUri("http://localhost:" + port));
    }

    @Test
    void shouldAuthenticateAndProxyMcpToolCallEndToEnd() throws Exception {
        String activeInstanceId = UUID.randomUUID().toString();
        try {
            // given.
            // language=json
            String beansResponse = """
                    {
                      "beans": [
                        {
                          "beanName": "testBean"
                        }
                      ]
                    }
                    """;
            mockWebServer.setDispatcher(new Dispatcher() {
                @Override
                public @NotNull MockResponse dispatch(@NotNull RecordedRequest request) {
                    String path = request.getPath();

                    if (path != null && path.equals("/" + activeInstanceId + "/actuator/axelix-beans")) {
                        return new MockResponse()
                                .setResponseCode(200)
                                .setBody(beansResponse)
                                .addHeader(HttpHeaders.CONTENT_TYPE, ACTUATOR_RESPONSE_CONTENT_TYPE);
                    }

                    return new MockResponse().setResponseCode(404);
                }
            });
            instanceRegistry.register(
                    TestObjectFactory.withUrl(activeInstanceId, mockWebServer.url(activeInstanceId) + "/actuator"));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set(HttpHeaders.AUTHORIZATION, "Basic " + basicCredentials("admin", "admin"));
            headers.set(HttpHeaders.ACCEPT, "text/event-stream, application/json");
            headers.set(MCP_PROTOCOL_VERSION_HEADER, MCP_PROTOCOL_VERSION);

            // language=json
            String toolsCallJsonRpcRequest = """
                    {
                      "jsonrpc": "2.0",
                      "id": 1,
                      "method": "tools/call",
                      "params": {
                        "name": "getInstanceBeans",
                        "arguments": {
                          "instanceId": "%s"
                        }
                      }
                    }
                    """
                    .formatted(activeInstanceId);
            ResponseEntity<String> response =
                    restTemplate.postForEntity("/api/mcp", new HttpEntity<>(toolsCallJsonRpcRequest, headers), String.class);

            // then.
            assertThat(response.getStatusCode().is2xxSuccessful())
                    .as("tools/call response status=%s body=%s", response.getStatusCode(), response.getBody())
                    .isTrue();
            assertThat(response.getBody()).contains("testBean");

            RecordedRequest proxiedRequest = mockWebServer.takeRequest(3, TimeUnit.SECONDS);
            assertThat(proxiedRequest).isNotNull();
            assertThat(proxiedRequest.getPath()).isEqualTo("/" + activeInstanceId + "/actuator/axelix-beans");
        } finally {
            instanceRegistry.deRegister(com.axelixlabs.axelix.master.domain.InstanceId.of(activeInstanceId));
        }
    }

    private String basicCredentials(String username, String password) {
        String credentials = username + ":" + password;
        return Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));
    }
}
