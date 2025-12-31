package com.nucleonforge.axelix.master;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import com.nucleonforge.axile.common.auth.rbac.core.Authority;
import com.nucleonforge.axile.common.auth.rbac.core.DefaultRole;
import com.nucleonforge.axile.common.auth.rbac.core.DefaultUser;
import com.nucleonforge.axile.master.autoconfiguration.auth.CookieProperties;
import com.nucleonforge.axile.master.service.auth.jwt.JwtEncoderService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

/**
 * Configuration for the tests that cover the HTTP API side.
 *
 * @author Mikhail Polivakha
 */
@Component
public class TestRestTemplateBuilder {

    @LocalServerPort
    private int testPort;

    @Autowired
    private CookieProperties cookieProperties;

    @Autowired
    private JwtEncoderService jwtEncoderService;

    public TestRestTemplate withAuthorities(Authority... authorities) {
        String token = generateToken(authorities);

        return new TestRestTemplate(
            new RestTemplateBuilder()
                .rootUri("http://localhost:" + testPort)
                .defaultHeader(HttpHeaders.SET_COOKIE, "%s=%s".formatted(cookieProperties.getName(), token))
        );
    }

    private String generateToken(Authority[] authorities) {
        return jwtEncoderService.generateToken(
            new DefaultUser(
                "testUser",
                "testPassword",
                Set.of(
                    new DefaultRole(
                        "testRole",
                        Arrays.stream(authorities).collect(Collectors.toSet()), Set.of())
                )
            )
        );
    }
}
