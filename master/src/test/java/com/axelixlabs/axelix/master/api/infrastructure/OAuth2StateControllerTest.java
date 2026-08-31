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

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.boot.restclient.RestTemplateBuilder;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;

import com.axelixlabs.axelix.master.api.infrastructure.OAuth2StateController.OAuth2StateResponse;
import com.axelixlabs.axelix.master.autoconfiguration.auth.properties.CookieProperties;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link OAuth2StateController}.
 *
 * @since 28.08.2026
 * @author Nikita Kirillov
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(
        properties = {
            "axelix.master.auth.options.oauth2.enabled=true",
            "axelix.master.auth.options.oauth2.issuer-uri=http://placeholder.will.be.overridden",
            "axelix.master.auth.options.oauth2.client-id=test-client",
            "axelix.master.auth.options.oauth2.client-secret=test-secret",
            "axelix.master.auth.options.oauth2.base-url=http://localhost:3000",
            "axelix.master.auth.options.oauth2.state-required=true"
        })
class OAuth2StateControllerTest {

    @LocalServerPort
    private int port;

    private TestRestTemplate restTemplate;

    @BeforeEach
    void prepare() {
        restTemplate = new TestRestTemplate(new RestTemplateBuilder());
    }

    @Test
    void shouldGenerateFreshUnguessableStateAndSetItAsUncacheableCookie() {
        // when.
        ResponseEntity<OAuth2StateResponse> first = generateState();
        ResponseEntity<OAuth2StateResponse> second = generateState();

        // then.
        assertThat(first.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(first.getHeaders().getCacheControl()).isEqualTo("no-store");

        String firstState = first.getBody().state();
        String secondState = second.getBody().state();

        assertThat(firstState).isNotBlank();
        assertThat(secondState).isNotBlank().isNotEqualTo(firstState);

        List<String> cookies = first.getHeaders().get(HttpHeaders.SET_COOKIE);
        String stateCookie = cookies.stream()
                .filter(it -> it.contains(CookieProperties.OAUTH2_STATE_COOKIE_NAME))
                .findFirst()
                .orElseThrow();

        assertThat(stateCookie).contains(firstState).contains("HttpOnly");
    }

    private ResponseEntity<OAuth2StateResponse> generateState() {
        return restTemplate.getForEntity(
                "http://localhost:" + port + "/api/external/oauth2/state", OAuth2StateResponse.class);
    }
}
