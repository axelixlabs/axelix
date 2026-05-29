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

import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.http.client.HttpRedirects;
import org.springframework.boot.restclient.RestTemplateBuilder;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.axelixlabs.axelix.common.auth.core.Authority;
import com.axelixlabs.axelix.common.auth.core.DefaultAuthority;
import com.axelixlabs.axelix.common.auth.core.DefaultRole;
import com.axelixlabs.axelix.common.auth.core.PasswordlessUser;
import com.axelixlabs.axelix.common.auth.exception.InvalidJwtTokenException;
import com.axelixlabs.axelix.common.auth.service.JwtDecoderService;
import com.axelixlabs.axelix.master.domain.UserEntity;
import com.axelixlabs.axelix.master.domain.UserOrigin;
import com.axelixlabs.axelix.master.exception.auth.OidcMetadataUnavailableException;
import com.axelixlabs.axelix.master.exception.auth.OidcTokenExchangeException;
import com.axelixlabs.axelix.master.repository.UserRepository;
import com.axelixlabs.axelix.master.service.auth.oauth.OidcClient;
import com.axelixlabs.axelix.master.service.auth.oauth.OidcRoleExtractor;
import com.axelixlabs.axelix.master.service.auth.oauth.Tokens;
import com.axelixlabs.axelix.master.service.state.UserService;

import static com.axelixlabs.axelix.master.autoconfiguration.mcp.McpAutoConfiguration.MCP_CONFIGURATION_PROPERTIES_PREFIX;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * Integration tests for {@link OAuth2CallbackController}.
 *
 * @since 06.03.2026
 * @author Nikita Kirillov
 * @author Mikhail Polivakha
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(
        properties = {
            MCP_CONFIGURATION_PROPERTIES_PREFIX + ".enabled=true",
            "axelix.master.auth.options.oauth2.enabled=true",
            "axelix.master.auth.options.oauth2.issuer-uri=http://placeholder.will.be.overridden",
            "axelix.master.auth.options.oauth2.client-id=test-client",
            "axelix.master.auth.options.oauth2.client-secret=test-secret",
            "axelix.master.auth.options.oauth2.base-url=http://localhost:3000",
            "axelix.master.auth.options.oauth2.role-attribute-path=roles[0]"
        })
class OAuth2CallbackControllerTest {

    private static final String CODE = "test-code";
    private static final String ID_TOKEN = "test-id-token";
    private static final String ACCESS_TOKEN = "test-access-token";
    private static final Tokens tokens = new Tokens(ID_TOKEN, ACCESS_TOKEN);

    @LocalServerPort
    private int port;

    private TestRestTemplate restTemplate;

    @MockitoBean
    private OidcClient oidcClient;

    @MockitoBean
    private OidcRoleExtractor oidcRoleExtractor;

    @Autowired
    private JwtDecoderService jwtDecoderService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @BeforeEach
    void prepare() {
        restTemplate = new TestRestTemplate(new RestTemplateBuilder().redirects(HttpRedirects.DONT_FOLLOW));
        userRepository.deleteAll();
    }

    @AfterEach
    void cleanUp() {
        userRepository.deleteAll();
    }

    @Test
    void happyPath() {
        // given.
        String username = "test-user";
        String email = "example@gmail.com";
        // language=json
        String userInfoJson = "{\"email\": \"%s\"}".formatted(email);

        // and.
        when(oidcClient.exchangeCodeForTokens(CODE)).thenReturn(tokens);
        when(oidcClient.validateIdTokenAndExtractUsername(ID_TOKEN)).thenReturn(username);
        when(oidcClient.validateAccessTokenAndExtractUserInfo(ACCESS_TOKEN)).thenReturn(userInfoJson);
        when(oidcRoleExtractor.extractRole(userInfoJson)).thenReturn(DefaultRole.EDITOR);

        // when.
        ResponseEntity<Void> response = restTemplate.getForEntity(
                "http://localhost:" + port + "/api/external/oauth2/callback?code=" + CODE, Void.class);

        // then.
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FOUND);
        assertThat(response.getHeaders().getLocation()).hasToString("/wallboard");

        List<String> cookies = response.getHeaders().get(HttpHeaders.SET_COOKIE);

        String authTokenCookie = findCookie(cookies, "auth_token");
        String authoritiesCookie = findCookie(cookies, "authorities");

        PasswordlessUser decodedTokenToUser = jwtDecoderService.decodeTokenToUser(
                // trying to extract an actual token from the cookie value
                authTokenCookie.substring(authTokenCookie.indexOf("=") + 1, authTokenCookie.indexOf(";")));

        assertThat(decodedTokenToUser.getUsername()).isEqualTo(username);
        assertThat(decodedTokenToUser.getRoles()).hasSize(1).first().isEqualTo(DefaultRole.EDITOR);

        String decodedAuthorities = assertThat(
                        // trying to extract an actual token from the cookie value
                        authoritiesCookie.substring(authoritiesCookie.indexOf("=") + 1, authoritiesCookie.indexOf(";")))
                .isBase64()
                .asBase64Decoded()
                .asString()
                .actual();

        DefaultRole.EDITOR.getAuthorities().stream()
                .map(Authority::getName)
                .forEach(name -> assertThat(decodedAuthorities).contains(name));

        assertThat(authoritiesCookie)
                .doesNotContain(
                        DefaultAuthority.ENV_VALUES_READ.getName(),
                        DefaultAuthority.CONFIG_PROPS_VALUES_READ.getName());

        // Verify that the OIDC user was correctly provisioned and persisted in the database
        UserEntity userEntity = userRepository.findByUsername(username).get();
        assertThat(userEntity.id()).isNotNull();
        assertThat(userEntity.username()).isEqualTo(username);
        assertThat(userEntity.email()).isEqualTo(email);
        assertThat(userEntity.password()).isNull();
        assertThat(userEntity.roles().values()).hasSize(1).containsOnly("EDITOR");
        assertThat(userEntity.userOrigin()).isEqualTo(UserOrigin.OIDC);
        assertThat(userEntity.lastLoginAt()).isNotNull();
    }

    @Test
    void shouldReturn400WhenOidcUserConflictsWithExistingLocalAccount() {
        // given.
        String username = "test-user";
        userService.create(username, null, null, "ADMIN", UserOrigin.LOCAL);

        // and.
        String userInfoJson = "someJson";
        when(oidcClient.exchangeCodeForTokens(CODE)).thenReturn(tokens);
        when(oidcClient.validateIdTokenAndExtractUsername(ID_TOKEN)).thenReturn(username);
        when(oidcClient.validateAccessTokenAndExtractUserInfo(ACCESS_TOKEN)).thenReturn(userInfoJson);
        when(oidcRoleExtractor.extractRole(userInfoJson)).thenReturn(DefaultRole.EDITOR);

        // when.
        ResponseEntity<Void> response = restTemplate.getForEntity(
                "http://localhost:" + port + "/api/external/oauth2/callback?code=" + CODE, Void.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void shouldReturn400WhenCodeParamMissing() {
        // when.
        ResponseEntity<Void> response =
                restTemplate.getForEntity("http://localhost:" + port + "/api/external/oauth2/callback", Void.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void shouldReturn401WhenCodeExchangeFails() {
        when(oidcClient.exchangeCodeForTokens(CODE)).thenThrow(new OidcTokenExchangeException("exchange failed"));

        // when.
        ResponseEntity<Void> response = restTemplate.getForEntity(
                "http://localhost:" + port + "/api/external/oauth2/callback?code=" + CODE, Void.class);

        // then.
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void shouldReturn401WhenIdTokenValidationFails() {
        when(oidcClient.exchangeCodeForTokens(CODE)).thenReturn(tokens);
        when(oidcClient.validateIdTokenAndExtractUsername(ID_TOKEN))
                .thenThrow(new InvalidJwtTokenException("invalid token"));

        // when.
        ResponseEntity<Void> response = restTemplate.getForEntity(
                "http://localhost:" + port + "/api/external/oauth2/callback?code=" + CODE, Void.class);

        // then.
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void shouldReturn502WhenUserInfoEndpointUnavailable() {
        when(oidcClient.exchangeCodeForTokens(CODE)).thenReturn(tokens);
        when(oidcClient.validateAccessTokenAndExtractUserInfo(ACCESS_TOKEN))
                .thenThrow(new OidcMetadataUnavailableException("metadata unavailable"));

        // when.
        ResponseEntity<Void> response = restTemplate.getForEntity(
                "http://localhost:" + port + "/api/external/oauth2/callback?code=" + CODE, Void.class);

        // then.
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_GATEWAY);
    }

    private static @NonNull String findCookie(List<String> cookies, String s) {
        return cookies.stream().filter(it -> it.contains(s)).findFirst().orElseThrow();
    }
}
