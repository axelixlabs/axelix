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
package com.nucleonforge.axelix.master;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.boot.web.servlet.context.ServletWebServerInitializedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

import com.nucleonforge.axelix.common.auth.core.Authority;
import com.nucleonforge.axelix.common.auth.core.DefaultRole;
import com.nucleonforge.axelix.common.auth.core.DefaultUser;
import com.nucleonforge.axelix.master.autoconfiguration.auth.CookieProperties;
import com.nucleonforge.axelix.master.service.auth.jwt.JwtEncoderService;

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
 */
@Component
public class TestRestTemplateBuilder {

    // We cannot use @LocalServerPort here since at the time of this
    // bean initialization, the webserver is not yet started, so, we
    // kind of have to lean towards a listener here.
    private int testTomcatServerPort;

    @EventListener
    public void handleServletWebServerInitializedEvent(ServletWebServerInitializedEvent event) {
        this.testTomcatServerPort = event.getWebServer().getPort();
    }

    @Autowired
    private CookieProperties cookieProperties;

    @Autowired
    private JwtEncoderService jwtEncoderService;

    public TestRestTemplate withoutAuthorities() {
        return withAuthorities();
    }

    public TestRestTemplate withAuthorities(Authority... authorities) {
        String token = generateToken(authorities);

        return new TestRestTemplate(new RestTemplateBuilder()
                .rootUri("http://localhost:" + testTomcatServerPort)
                .defaultHeader(HttpHeaders.COOKIE, "%s=%s".formatted(cookieProperties.getName(), token)));
    }

    private String generateToken(Authority[] authorities) {
        return jwtEncoderService.generateToken(new DefaultUser(
                "testUser",
                "testPassword",
                Set.of(new DefaultRole("testRole", Arrays.stream(authorities).collect(Collectors.toSet()), Set.of()))));
    }
}
