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

import java.time.Instant;
import java.util.Date;

import io.jsonwebtoken.Jwts;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.boot.web.servlet.context.ServletWebServerInitializedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

import com.axelixlabs.axelix.common.auth.core.AuthenticationSchemes;
import com.axelixlabs.axelix.common.auth.core.JwtAlgorithm;
import com.axelixlabs.axelix.common.auth.core.Role;
import com.axelixlabs.axelix.common.auth.core.TechUser;
import com.axelixlabs.axelix.common.auth.service.JwtSigningStrategy;
import com.axelixlabs.axelix.common.auth.service.JwtSigningStrategyFactory;
import com.axelixlabs.axelix.common.auth.service.tech.DefaultTechUserJwtSerializer;
import com.axelixlabs.axelix.common.auth.service.tech.TechUserJwtSerializer;
import com.axelixlabs.axelix.common.testfixtures.TestRoles;

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

    // We cannot use @LocalServerPort here since at the time of this
    // bean initialization, the webserver is not yet started, so, we
    // kind of have to lean towards a listener here.
    private int testTomcatServerPort;

    private final TechUserJwtSerializer techUserJwtSerializer;
    private final JwtSigningStrategy signingStrategy;
    private final String signingKey;

    @EventListener
    public void handleServletWebServerInitializedEvent(ServletWebServerInitializedEvent event) {
        this.testTomcatServerPort = event.getWebServer().getPort();
    }

    public TestRestTemplateBuilder(
            final @Value("${axelix.sbs.auth.jwt.algorithm}") JwtAlgorithm algorithm,
            final @Value("${axelix.sbs.auth.jwt.signing-key}") String signingKey) {
        this.techUserJwtSerializer = new DefaultTechUserJwtSerializer(algorithm, signingKey);
        this.signingStrategy = JwtSigningStrategyFactory.createSigningStrategy(algorithm);
        this.signingKey = signingKey;
    }

    public TestRestTemplate asViewer() {
        return withRole(TestRoles.VIEWER);
    }

    public TestRestTemplate asEditor() {
        return withRole(TestRoles.EDITOR);
    }

    public TestRestTemplate asAdmin() {
        return withRole(TestRoles.ADMIN);
    }

    public TestRestTemplate withRole(Role role) {
        String token = techUserJwtSerializer.serialize(TechUser.MASTER);

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

    public TestRestTemplate withExpiredTokenInAuthHeader() {
        String expiredToken = generateExpiredToken();

        return buildWithToken(expiredToken);
    }

    public TestRestTemplate withMalformedTokenInAuthHeader() {
        String malformedToken = "malformed token";

        return buildWithToken(malformedToken);
    }

    public TestRestTemplate withoutToken() {
        return new TestRestTemplate(new RestTemplateBuilder().rootUri(HOST + testTomcatServerPort));
    }
    // END: Bad token auth scenarios

    private TestRestTemplate buildWithToken(String expiredToken) {
        return new TestRestTemplate(new RestTemplateBuilder()
                .rootUri(HOST + testTomcatServerPort)
                .defaultHeader(HttpHeaders.AUTHORIZATION, AuthenticationSchemes.BEARER.prefix() + expiredToken));
    }

    private String generateExpiredToken() {
        return signingStrategy
                .signToken(
                        Jwts.builder()
                                .subject(TechUser.MASTER.getId())
                                .issuedAt(Date.from(Instant.now().minusSeconds(120)))
                                .expiration(Date.from(Instant.now().minusSeconds(60))),
                        signingKey)
                .compact();
    }
}
