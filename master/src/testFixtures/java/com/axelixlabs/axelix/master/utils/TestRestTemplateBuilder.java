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
package com.axelixlabs.axelix.master.utils;

import java.time.Duration;

import org.springframework.boot.restclient.RestTemplateBuilder;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.web.server.servlet.context.ServletWebServerInitializedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

import com.axelixlabs.axelix.common.auth.core.AuthenticationSchemes;
import com.axelixlabs.axelix.common.auth.core.Role;
import com.axelixlabs.axelix.common.auth.core.User;
import com.axelixlabs.axelix.common.auth.service.DefaultJwtEncoderService;
import com.axelixlabs.axelix.common.auth.service.JwtEncoderService;
import com.axelixlabs.axelix.common.testfixtures.TestRoles;
import com.axelixlabs.axelix.common.testfixtures.UserUtils;
import com.axelixlabs.axelix.master.autoconfiguration.auth.properties.CookieProperties;
import com.axelixlabs.axelix.master.autoconfiguration.auth.properties.JwtProperties;

/**
 * Configuration for the tests that cover the HTTP API side.
 *
 * TODO:
 *  I would like to do it via extending the {@link TestRestTemplate}, really, honestly,
 *  I do. It would really hide the complexity of injecting the cookie and minimize the
 *  changes in our codebase. I understand that.
 *  But creating a delegate for the TestRestTemplate is such a pain in the ass, and I
 *  really hope that Brain Goetz would hear my cry about delegates being a native feature
 *  of Java similarly to what is currently done in Kotlin. Man, I miss it so much...
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

    private final JwtEncoderService defaultJwtEncoderService;
    private final JwtEncoderService expiredJwtEncoderService;

    @EventListener
    public void handleServletWebServerInitializedEvent(ServletWebServerInitializedEvent event) {
        this.testTomcatServerPort = event.getWebServer().getPort();
    }

    public TestRestTemplateBuilder(JwtProperties jwtProperties, JwtEncoderService defaultJwtEncoderService) {
        this.defaultJwtEncoderService = defaultJwtEncoderService;
        this.expiredJwtEncoderService =
                new DefaultJwtEncoderService(jwtProperties.algorithm(), jwtProperties.signingKey(), Duration.ZERO);
    }

    public IdentityAwareTestRestTemplate asViewer() {
        return withRole(TestRoles.VIEWER);
    }

    public IdentityAwareTestRestTemplate asEditor() {
        return withRole(TestRoles.EDITOR);
    }

    public IdentityAwareTestRestTemplate withRole(Role role) {
        User user = UserUtils.fromRoles(role);

        String token = defaultJwtEncoderService.generateToken(user);

        return buildWithToken(user, token);
    }

    public IdentityAwareTestRestTemplate withRoleTokenInAuthorizationHeader(Role role) {
        User user = UserUtils.fromRoles(role);

        String token = defaultJwtEncoderService.generateToken(user);

        return buildWithTokenInAuthorizationHeader(user, token);
    }

    // START: Bad token auth scenarios
    public TestRestTemplate withExpiredToken() {
        User user = UserUtils.fromRoles();

        String expiredToken = expiredJwtEncoderService.generateToken(user);

        return buildWithToken(user, expiredToken);
    }

    public TestRestTemplate withMalformedToken() {
        String malformedToken = "malformed token";

        return buildWithToken(malformedToken);
    }

    public TestRestTemplate withExpiredTokenInAuthHeader() {
        String expiredToken = generateExpiredToken();

        return buildWithTokenInAuthorizationHeader(expiredToken);
    }

    public TestRestTemplate withMalformedTokenInAuthHeader() {
        String malformedToken = "malformed token";

        return buildWithTokenInAuthorizationHeader(malformedToken);
    }

    public TestRestTemplate withoutToken() {
        return new TestRestTemplate(new RestTemplateBuilder().baseUri(HOST + testTomcatServerPort));
    }
    // END: Bad token auth scenarios

    private IdentityAwareTestRestTemplate buildWithToken(User user, String token) {
        return new IdentityAwareTestRestTemplate(
                user,
                new RestTemplateBuilder()
                        .baseUri(HOST + testTomcatServerPort)
                        .defaultHeader(
                                HttpHeaders.COOKIE, "%s=%s".formatted(CookieProperties.AUTH_COOKIE_NAME, token)));
    }

    private TestRestTemplate buildWithToken(String token) {
        return new TestRestTemplate(new RestTemplateBuilder()
                .baseUri(HOST + testTomcatServerPort)
                .defaultHeader(HttpHeaders.COOKIE, "%s=%s".formatted(CookieProperties.AUTH_COOKIE_NAME, token)));
    }

    private IdentityAwareTestRestTemplate buildWithTokenInAuthorizationHeader(User user, String token) {
        return new IdentityAwareTestRestTemplate(
                user,
                new RestTemplateBuilder()
                        .baseUri(HOST + testTomcatServerPort)
                        .defaultHeader(HttpHeaders.AUTHORIZATION, AuthenticationSchemes.BEARER.prefix() + token));
    }

    private TestRestTemplate buildWithTokenInAuthorizationHeader(String token) {
        return new TestRestTemplate(new RestTemplateBuilder()
                .baseUri(HOST + testTomcatServerPort)
                .defaultHeader(HttpHeaders.AUTHORIZATION, AuthenticationSchemes.BEARER.prefix() + token));
    }

    private String generateExpiredToken() {
        return expiredJwtEncoderService.generateToken(UserUtils.fromRoles());
    }
}
