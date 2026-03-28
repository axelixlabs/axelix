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
package com.axelixlabs.axelix.master.filter;

import java.util.Base64;

import io.modelcontextprotocol.server.McpSyncServer;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.axelixlabs.axelix.master.exception.auth.OidcTokenExchangeException;
import com.axelixlabs.axelix.master.service.auth.UserLoginService;
import com.axelixlabs.axelix.master.service.auth.oauth.OidcClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doThrow;

/**
 * Integration tests for {@link McpAuthenticationFilter}
 *
 * @author Nikita Kirillov
 */
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@TestPropertySource(
        properties = {
            "axelix.master.auth.options.oauth2.enabled=true",
            "axelix.master.auth.options.oauth2.issuer-uri=http://localhost:8081/realms/axelix",
            "axelix.master.auth.options.oauth2.base-url=http://localhost:8080",
            "axelix.master.auth.options.oauth2.client-id=clientId",
            "axelix.master.auth.options.oauth2.client-secret=clientSecret",
            "axelix.master.auth.options.static-admin.enabled=true",
            "axelix.master.auth.options.static-admin.credentials.username=admin",
            "axelix.master.auth.options.static-admin.credentials.password=admin",
        })
@AutoConfigureTestRestTemplate
class McpAuthenticationFilterTest {

    // Used to simulate MCP client initialization
    // language=json
    private static final String INITIALIZE_REQUEST_BODY = """
        {
            "jsonrpc": "2.0",
            "id": 1,
            "method": "initialize",
            "params": {
                "protocolVersion": "1.0.1"
            }
        }
        """;
    private static final String MCP_API_PATH = "/api/mcp";
    private static final String USERNAME = "admin";
    private static final String PASSWORD = "admin";
    private static final String CREDENTIALS = USERNAME + ":" + PASSWORD;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private McpSyncServer mcpSyncServer;

    @Autowired
    private UserLoginService userLoginService;

    @MockitoBean
    private OidcClient oidcClient;

    private final Base64.Encoder encoder = Base64.getEncoder();

    @Test
    void shouldAllowRequestWithValidBasicAuth() {
        String encodedCredentials = encoder.encodeToString(CREDENTIALS.getBytes());

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Basic " + encodedCredentials);
        headers.set("Accept", "text/event-stream, application/json");
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> request = new HttpEntity<>(INITIALIZE_REQUEST_BODY, headers);

        ResponseEntity<String> response = restTemplate.exchange(MCP_API_PATH, HttpMethod.POST, request, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void shouldRejectInvalidBasicAuth() {
        String credentials = "wrong:wrong";
        String encodedCredentials = encoder.encodeToString(credentials.getBytes());

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Basic " + encodedCredentials);

        HttpEntity<Void> request = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(MCP_API_PATH, HttpMethod.GET, request, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody()).contains("Invalid username or password");
    }

    @Test
    void shouldAllowRequestWithValidBearerToken() {
        String validToken = "valid-jwt-token";

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + validToken);
        headers.set("Accept", "text/event-stream, application/json");
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> request = new HttpEntity<>(INITIALIZE_REQUEST_BODY, headers);

        ResponseEntity<String> response = restTemplate.exchange(MCP_API_PATH, HttpMethod.POST, request, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void shouldRejectInvalidBearerToken() {
        String invalidToken = "invalid-token";

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + invalidToken);

        HttpEntity<Void> request = new HttpEntity<>(headers);

        doThrow(new OidcTokenExchangeException("Invalid token"))
                .when(oidcClient)
                .validateTokenViaUserInfoEndpoint(invalidToken);

        ResponseEntity<String> response = restTemplate.exchange(MCP_API_PATH, HttpMethod.GET, request, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getHeaders().get("WWW-Authenticate")).isNotEmpty();
    }

    @Test
    void shouldRejectRequestWithoutAuthHeader() {
        HttpEntity<Void> request = new HttpEntity<>(new HttpHeaders());

        ResponseEntity<String> response = restTemplate.exchange(MCP_API_PATH, HttpMethod.GET, request, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getHeaders().get("WWW-Authenticate")).isNotEmpty();
    }

    @Test
    void shouldRejectUnsupportedAuthScheme() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Any token");

        HttpEntity<Void> request = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(MCP_API_PATH, HttpMethod.GET, request, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody()).contains("Unsupported authorization scheme");
    }
}
