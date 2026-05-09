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
package com.axelixlabs.axelix.master.api.infrastructure;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;

/**
 * Integration tests for {@link McpOAuth2MetadataController}
 *
 * @author Nikita Kirillov
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(
        properties = {
            "axelix.master.auth.options.oauth2.enabled=true",
            "axelix.master.auth.options.oauth2.issuer-uri=http://localhost:8081/realms/axelix",
            "axelix.master.auth.options.oauth2.client-id=test-client",
            "axelix.master.auth.options.oauth2.client-secret=test-secret",
            "axelix.master.auth.options.oauth2.base-url=http://localhost:8080"
        })
@AutoConfigureTestRestTemplate
class McpOAuth2MetadataControllerTest {

    private static final String EXPECTED_JSON =
            // language=json
            """
   {
     "authorization_servers" : [ "http://localhost:8081/realms/axelix" ],
     "resource" : "http://localhost:8080/api/external/api/mcp",
     "scopes_supported" : [ "openid" ]
   }
   """;

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void shouldReturnOAuthProtectedResource() {
        ResponseEntity<String> response = restTemplate.getForEntity(
                "/api/external/mcp-oauth2/.well-known/oauth-protected-resource", String.class);

        assertThatJson(response.getBody()).isEqualTo(EXPECTED_JSON);
    }
}
