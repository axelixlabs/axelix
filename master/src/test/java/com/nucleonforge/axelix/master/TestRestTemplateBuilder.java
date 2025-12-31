/*
 * Copyright 2025-present, Nucleon Forge Software.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
