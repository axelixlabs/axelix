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
import java.util.stream.Stream;

import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.restclient.RestTemplateBuilder;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.axelixlabs.axelix.common.auth.core.DefaultRole;
import com.axelixlabs.axelix.master.autoconfiguration.mcp.McpAutoConfiguration;
import com.axelixlabs.axelix.master.domain.UserOrigin;
import com.axelixlabs.axelix.master.exception.auth.OidcTokenExchangeException;
import com.axelixlabs.axelix.master.repository.InstanceRepository;
import com.axelixlabs.axelix.master.repository.UserRepository;
import com.axelixlabs.axelix.master.service.auth.oauth.OidcRoleExtractor;
import com.axelixlabs.axelix.master.service.state.InstanceRegistry;
import com.axelixlabs.axelix.master.service.state.UserService;
import com.axelixlabs.axelix.master.utils.TestObjectFactory;

import static com.axelixlabs.axelix.master.utils.ContentType.ACTUATOR_RESPONSE_CONTENT_TYPE;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.of;
import static org.mockito.Mockito.when;

/**
 * Integration tests for {@link McpAuthorizationFilter}.
 *
 * @author Mikhail Polivakha
 */
abstract class AbstractMcpAuthorizationFilterTest {

    private static final String MCP_PROTOCOL_VERSION_HEADER = "MCP-Protocol-Version";

    private static final String MCP_SESSION_ID_HEADER = "mcp-session-id";

    private static final String MCP_PROTOCOL_VERSION = "2024-11-05";

    private static MockWebServer mockWebServer;

    @Autowired
    protected InstanceRegistry instanceRegistry;

    @LocalServerPort
    protected int port;

    protected TestRestTemplate restTemplate;

    @Autowired
    protected InstanceRepository instanceRepository;

    @Autowired
    protected UserService userService;

    @Autowired
    protected UserRepository userRepository;

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
        instanceRepository.deleteAll();
        userRepository.deleteAll();
        this.restTemplate = new TestRestTemplate(new RestTemplateBuilder().rootUri("http://localhost:" + port));
    }

    protected void registerInstanceForBeansTool(String activeInstanceId) {
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
    }

    @SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
    @Import(McpAutoConfiguration.class)
    @TestPropertySource(
            properties = {
                "axelix.master.mcp-server.enabled=true",
                "axelix.master.auth.options.local.enabled=true",
                "axelix.master.auth.options.super-admin.credentials.username=admin",
                "axelix.master.auth.options.super-admin.credentials.password=admin",
            })
    static class BasicAuthTest extends AbstractMcpAuthorizationFilterTest {

        @Test
        void shouldAuthenticateAsSuperAdminAndProxyMcpToolCallEndToEnd() {
            String activeInstanceId = UUID.randomUUID().toString();

            // given.
            registerInstanceForBeansTool(activeInstanceId);
            HttpHeaders headers = commonMcpHeaders();
            headers.set(HttpHeaders.AUTHORIZATION, "Basic " + basicCredentials("admin", "admin"));
            String mcpSessionId = initializeMcpSession(restTemplate, headers);
            headers.set(MCP_SESSION_ID_HEADER, mcpSessionId);

            // when.
            ResponseEntity<String> response = restTemplate.postForEntity(
                    "/api/mcp", new HttpEntity<>(buildToolsCallRequest(activeInstanceId), headers), String.class);

            // then.
            assertSuccessfulToolCallResponse(response);
        }

        @Test
        void shouldAuthenticateAsDatabaseUserAndProxyMcpToolCallEndToEnd() {
            // given.
            String activeInstanceId = UUID.randomUUID().toString();
            String username = "test-user";
            String password = "test-password";

            userService.create(
                    username, "test-email@example.com", password, DefaultRole.VIEWER.getName(), UserOrigin.LOCAL);

            // and.
            registerInstanceForBeansTool(activeInstanceId);
            HttpHeaders headers = commonMcpHeaders();
            headers.set(HttpHeaders.AUTHORIZATION, "Basic " + basicCredentials(username, password));
            String mcpSessionId = initializeMcpSession(restTemplate, headers);
            headers.set(MCP_SESSION_ID_HEADER, mcpSessionId);

            // when.
            ResponseEntity<String> response = restTemplate.postForEntity(
                    "/api/mcp", new HttpEntity<>(buildToolsCallRequest(activeInstanceId), headers), String.class);

            // then.
            assertSuccessfulToolCallResponse(response);
        }

        @ParameterizedTest
        @MethodSource("typicalMcpRequests")
        void shouldReturnUnauthorizedWhenAuthorizationHeaderIsMissing(String request) {
            // given.
            HttpHeaders headers = commonMcpHeaders();

            // when.
            ResponseEntity<String> response =
                    restTemplate.postForEntity("/api/mcp", new HttpEntity<>(request, headers), String.class);

            // then.
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        }

        @ParameterizedTest
        @MethodSource("typicalMcpRequests")
        void shouldReturnUnauthorizedWhenBasicCredentialsAreInvalid(String request) {
            // given.
            HttpHeaders headers = commonMcpHeaders();
            headers.set(HttpHeaders.AUTHORIZATION, "Basic " + basicCredentials("admin", "wrong-password"));

            // when.
            ResponseEntity<String> response =
                    restTemplate.postForEntity("/api/mcp", new HttpEntity<>(request, headers), String.class);

            // then.
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        }

        @Test
        void shouldReturnForbiddenWhenUserIsNotAuthorizedForEndpoint() {
            // given.
            String activeInstanceId = UUID.randomUUID().toString();
            String username = "viewer-user";
            String password = "viewer-password";
            userService.create(
                    username, username + "@example.com", password, DefaultRole.VIEWER.getName(), UserOrigin.LOCAL);

            HttpHeaders headers = commonMcpHeaders();
            headers.set(HttpHeaders.AUTHORIZATION, "Basic " + basicCredentials(username, password));

            // when.
            ResponseEntity<String> response = restTemplate.postForEntity(
                    "/api/mcp",
                    new HttpEntity<>(buildToolsCallRequest(activeInstanceId, "clearAllCaches"), headers),
                    String.class);

            // then.
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        }

        @Test
        void shouldReturnOkWhenViewerIsTryingToListMcpEndpoints() {
            // given.
            String username = "viewer-user";
            String password = "viewer-password";
            userService.create(
                    username, username + "@example.com", password, DefaultRole.VIEWER.getName(), UserOrigin.LOCAL);

            HttpHeaders headers = commonMcpHeaders();
            headers.set(HttpHeaders.AUTHORIZATION, "Basic " + basicCredentials(username, password));

            // when.
            ResponseEntity<String> response = restTemplate.postForEntity(
                    "/api/mcp", new HttpEntity<>(buildInitializeRequest(), headers), String.class);

            // then.
            assertSuccessfulInitializeResponse(response);
        }
    }

    @SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
    @Import(McpAutoConfiguration.class)
    @TestPropertySource(
            properties = {
                "axelix.master.mcp-server.enabled=true",
                "axelix.master.auth.options.oauth2.enabled=true",
                "axelix.master.auth.options.oauth2.issuer-uri=http://localhost:8999",
                "axelix.master.auth.options.oauth2.client-id=test-client-id",
                "axelix.master.auth.options.oauth2.client-secret=test-client-secret",
                "axelix.master.auth.options.oauth2.base-url=http://localhost:8080"
            })
    static class BearerAuthTest extends AbstractMcpAuthorizationFilterTest {

        @MockitoBean
        private OidcRoleExtractor oidcRoleExtractor;

        @Test
        void shouldAuthenticateAndProxyMcpToolCallEndToEnd() {
            String activeInstanceId = UUID.randomUUID().toString();
            String token = "editor-access-token";

            // given.
            registerInstanceForBeansTool(activeInstanceId);
            when(oidcRoleExtractor.extractRole(token)).thenReturn(DefaultRole.VIEWER);
            HttpHeaders headers = bearerAuthHeaders(token);
            String mcpSessionId = initializeMcpSession(restTemplate, headers);
            headers.set(MCP_SESSION_ID_HEADER, mcpSessionId);

            // when.
            ResponseEntity<String> response = restTemplate.postForEntity(
                    "/api/mcp", new HttpEntity<>(buildToolsCallRequest(activeInstanceId), headers), String.class);

            // then.
            assertSuccessfulToolCallResponse(response);
        }

        @ParameterizedTest
        @MethodSource("typicalMcpRequests")
        void shouldReturnUnauthorizedWhenAuthorizationHeaderIsMissing(String request) {
            // given.
            HttpHeaders headers = commonMcpHeaders();

            // when.
            ResponseEntity<String> response =
                    restTemplate.postForEntity("/api/mcp", new HttpEntity<>(request, headers), String.class);

            // then.
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
            assertThat(response.getHeaders().get(McpAuthorizationFilter.WWW_AUTHENTICATE_OAUTH2_HEADER))
                    .hasSize(1)
                    .first()
                    .isEqualTo(
                            "Bearer resource_metadata=\"http://localhost:8080/api/external/mcp-oauth2/.well-known/oauth-protected-resource\"");
        }

        @ParameterizedTest
        @MethodSource("typicalMcpRequests")
        void shouldReturnUnauthorizedWhenBearerTokenIsInvalid(String request) {
            // given.
            String token = "malformed-token";
            when(oidcRoleExtractor.extractRole(token)).thenThrow(new OidcTokenExchangeException("Malformed token"));
            HttpHeaders headers = bearerAuthHeaders(token);

            // when.
            ResponseEntity<String> response =
                    restTemplate.postForEntity("/api/mcp", new HttpEntity<>(request, headers), String.class);

            // then.
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
            assertThat(response.getHeaders().get(McpAuthorizationFilter.WWW_AUTHENTICATE_OAUTH2_HEADER))
                    .hasSize(1)
                    .first()
                    .isEqualTo(
                            "Bearer resource_metadata=\"http://localhost:8080/api/external/mcp-oauth2/.well-known/oauth-protected-resource\"");
        }

        @Test
        void shouldReturnForbiddenWhenUserIsNotAuthorizedForEndpoint() {
            // given.
            String activeInstanceId = UUID.randomUUID().toString();
            String token = "viewer-access-token";
            when(oidcRoleExtractor.extractRole(token)).thenReturn(DefaultRole.VIEWER);
            HttpHeaders headers = bearerAuthHeaders(token);

            // when.
            ResponseEntity<String> response = restTemplate.postForEntity(
                    "/api/mcp",
                    new HttpEntity<>(buildToolsCallRequest(activeInstanceId, "clearAllCaches"), headers),
                    String.class);

            // then.
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        }

        @Test
        void shouldReturnOkWhenViewerIsTryingToListMcpEndpoints() {
            // given.
            String token = "viewer-access-token";
            when(oidcRoleExtractor.extractRole(token)).thenReturn(DefaultRole.VIEWER);
            HttpHeaders headers = bearerAuthHeaders(token);

            // when.
            ResponseEntity<String> response = restTemplate.postForEntity(
                    "/api/mcp", new HttpEntity<>(buildInitializeRequest(), headers), String.class);

            // then.
            assertSuccessfulInitializeResponse(response);
        }

        private HttpHeaders bearerAuthHeaders(String token) {
            HttpHeaders headers = commonMcpHeaders();
            headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + token);
            return headers;
        }
    }

    static void assertSuccessfulInitializeResponse(ResponseEntity<String> response) {
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThatJson(
                        // language=json
                        """
                {
                  "jsonrpc" : "2.0",
                  "id" : 999,
                  "result" : {
                    "protocolVersion" : "%s",
                    "capabilities" : {
                      "completions" : { },
                      "logging" : { },
                      "prompts" : {
                        "listChanged" : true
                      },
                      "resources" : {
                        "subscribe" : false,
                        "listChanged" : true
                      },
                      "tools" : {
                        "listChanged" : true
                      }
                    },
                    "serverInfo" : {
                      "name" : "axelix-mcp-server",
                      "version" : "1.0.0"
                    }
                  }
                }
                """.formatted(MCP_PROTOCOL_VERSION))
                .isEqualTo(response.getBody());
    }

    static void assertSuccessfulToolCallResponse(ResponseEntity<String> response) {
        String sseData = parseSseData(response.getBody());

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThatJson(sseData)
                .isEqualTo(
                        // language=json
                        """
                {
                  "jsonrpc" : "2.0",
                  "id" : 1,
                  "result" : {
                    "content" : [ {
                      "type" : "text",
                      "text" : "{\\n  \\"beans\\": [\\n    {\\n      \\"beanName\\": \\"testBean\\"\\n    }\\n  ]\\n}\\n"
                    } ],
                    "isError" : false
                  }
                }
                """);
    }

    private static HttpHeaders commonMcpHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set(HttpHeaders.ACCEPT, "text/event-stream, application/json");
        headers.set(MCP_PROTOCOL_VERSION_HEADER, MCP_PROTOCOL_VERSION);
        return headers;
    }

    private static String initializeMcpSession(TestRestTemplate restTemplate, HttpHeaders authHeaders) {
        HttpHeaders initializeHeaders = commonMcpHeaders();
        initializeHeaders.set(HttpHeaders.AUTHORIZATION, authHeaders.getFirst(HttpHeaders.AUTHORIZATION));

        String initializeRequestBody = buildInitializeRequest();

        ResponseEntity<String> initializeResponse = restTemplate.postForEntity(
                "/api/mcp", new HttpEntity<>(initializeRequestBody, initializeHeaders), String.class);

        assertThat(initializeResponse.getStatusCode().value()).isEqualTo(HttpStatus.OK.value());

        String mcpSessionId = initializeResponse.getHeaders().getFirst(MCP_SESSION_ID_HEADER);
        assertThat(mcpSessionId).isNotBlank();
        return mcpSessionId;
    }

    public static Stream<Arguments> typicalMcpRequests() {
        return Stream.of(
                of(buildInitializeRequest()),
                of(buildToolsCallRequest(UUID.randomUUID().toString())));
    }

    private static String buildToolsCallRequest(String instanceId) {
        return buildToolsCallRequest(instanceId, "getInstanceBeans");
    }

    private static String buildToolsCallRequest(String instanceId, String toolName) {
        // language=json
        return """
            {
              "jsonrpc": "2.0",
              "id": 1,
              "method": "tools/call",
              "params": {
                "name": "%s",
                "arguments": {
                  "instanceId": "%s"
                }
              }
            }
            """.formatted(toolName, instanceId);
    }

    private static @NonNull String buildInitializeRequest() {
        // language=json
        return """
            {
              "jsonrpc": "2.0",
              "id": 999,
              "method": "initialize",
              "params": {
                "protocolVersion": "%s",
                "capabilities": {},
                "clientInfo": {
                  "name": "mcp-auth-filter-test",
                  "version": "1.0.0"
                }
              }
            }
            """.formatted(MCP_PROTOCOL_VERSION);
    }

    private static String basicCredentials(String username, String password) {
        String credentials = username + ":" + password;
        return Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));
    }

    private static String parseSseData(String event) {
        return event.lines()
                .filter(s -> s.trim().startsWith("data:"))
                .map(s -> s.substring("data:".length()).trim())
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Invalid SSE message format"));
    }
}
