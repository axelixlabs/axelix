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
package com.axelixlabs.axelix.sbs.spring.core.utils;

import java.time.Duration;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.boot.web.servlet.context.ServletWebServerInitializedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

import com.axelixlabs.axelix.common.auth.core.AuthenticationSchemes;
import com.axelixlabs.axelix.common.auth.core.DefaultRole;
import com.axelixlabs.axelix.common.auth.core.DefaultUser;
import com.axelixlabs.axelix.common.auth.core.JwtAlgorithm;
import com.axelixlabs.axelix.common.auth.core.Role;
import com.axelixlabs.axelix.common.auth.service.DefaultJwtEncoderService;
import com.axelixlabs.axelix.common.auth.service.JwtEncoderService;

/**
 * Configuration for the tests that cover the HTTP API side.
 *
 * @author Mikhail Polivakha
 * @author Sergey Cherkasov
 * @author Nikita Kirillov
 */
@Component
public class TestRestTemplateBuilder {

    private static final String HOST = "http://localhost:";
    private static final String USERNAME = "testUser";
    private static final String PASSWORD = "testPassword";

    // We cannot use @LocalServerPort here since at the time of this
    // bean initialization, the webserver is not yet started, so, we
    // kind of have to lean towards a listener here.
    private int testTomcatServerPort;

    private final JwtEncoderService defaultJwtEncoderService;
    private final JwtEncoderService expiredJwtEncoderService;

    @EventListener
    public void handleServletWebServerInitializedEvent(ServletWebServerInitializedEvent event) {
        this.testTomcatServerPort = event.getWebServer().getPort();
    }

    public TestRestTemplateBuilder(
            final @Value("${axelix.sbs.auth.jwt.algorithm}") JwtAlgorithm algorithm,
            final @Value("${axelix.sbs.auth.jwt.signing-key}") String signingKey) {
        this.defaultJwtEncoderService = new DefaultJwtEncoderService(algorithm, signingKey, Duration.ofHours(1));
        this.expiredJwtEncoderService = new DefaultJwtEncoderService(algorithm, signingKey, Duration.ZERO);
    }

    public TestRestTemplate asViewer() {
        return withRole(DefaultRole.VIEWER);
    }

    public TestRestTemplate asEditor() {
        return withRole(DefaultRole.EDITOR);
    }

    public TestRestTemplate asAdmin() {
        return withRole(DefaultRole.ADMIN);
    }

    public TestRestTemplate withRole(Role role) {
        String token = generateToken(new Role[] {role});

        return buildWithToken(token);
    }

    // START: Bad token auth scenarios
    TestRestTemplate withExpiredToken() {
        String expiredToken = generateExpiredToken();

        return buildWithToken(expiredToken);
    }

    TestRestTemplate withMalformedToken() {
        String malformedToken = "malformed token";

        return buildWithToken(malformedToken);
    }
    // END: Bad token auth scenarios

    private TestRestTemplate buildWithToken(String expiredToken) {
        return new TestRestTemplate(new RestTemplateBuilder()
                .rootUri(HOST + testTomcatServerPort)
                .defaultHeader(HttpHeaders.AUTHORIZATION, AuthenticationSchemes.BEARER.code() + " " + expiredToken));
    }

    private String generateToken(Role[] roles) {
        return defaultJwtEncoderService.generateToken(
                new DefaultUser(USERNAME, PASSWORD, Arrays.stream(roles).collect(Collectors.toSet())));
    }

    private String generateExpiredToken() {
        return expiredJwtEncoderService.generateToken(new DefaultUser(USERNAME, PASSWORD, Set.of()));
    }
}
