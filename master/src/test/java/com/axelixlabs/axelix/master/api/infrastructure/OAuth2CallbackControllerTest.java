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

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

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
import com.axelixlabs.axelix.common.auth.core.PasswordlessUser;
import com.axelixlabs.axelix.common.auth.exception.InvalidJwtTokenException;
import com.axelixlabs.axelix.common.auth.service.JwtDecoderService;
import com.axelixlabs.axelix.common.testfixtures.TestRoles;
import com.axelixlabs.axelix.master.domain.UserEntity;
import com.axelixlabs.axelix.master.domain.UserOrigin;
import com.axelixlabs.axelix.master.domain.UserStatus;
import com.axelixlabs.axelix.master.exception.auth.OAuth2AuthenticationException;
import com.axelixlabs.axelix.master.exception.auth.OidcMetadataUnavailableException;
import com.axelixlabs.axelix.master.exception.auth.OidcTokenExchangeException;
import com.axelixlabs.axelix.master.repository.UserRepository;
import com.axelixlabs.axelix.master.service.auth.MasterWebEndpoints;
import com.axelixlabs.axelix.master.service.auth.oauth.OidcClient;
import com.axelixlabs.axelix.master.service.auth.oauth.OidcIdTokenClaimsValidator;
import com.axelixlabs.axelix.master.service.auth.oauth.Tokens;
import com.axelixlabs.axelix.master.service.auth.oauth.UserInfoJsonAccessor;
import com.axelixlabs.axelix.master.service.auth.oauth.ValidatedOidcIdentity;
import com.axelixlabs.axelix.master.service.state.UserService;
import com.axelixlabs.axelix.master.utils.auth.AbstractProtectedEndpointTest;

import static com.axelixlabs.axelix.master.autoconfiguration.mcp.McpAutoConfiguration.MCP_CONFIGURATION_PROPERTIES_PREFIX;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doThrow;
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
            "axelix.master.auth.options.oauth2.role-attribute-path=roles[0]",
            "axelix.master.auth.options.oauth2.job-title-attribute-path=employment.jobTitle",
            "axelix.master.auth.options.oauth2.organizational-unit-attribute-path=employment.organizationalUnit"
        })
class OAuth2CallbackControllerTest extends AbstractProtectedEndpointTest {

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
    private UserInfoJsonAccessor userInfoJsonAccessor;

    @MockitoBean
    private OidcIdTokenClaimsValidator claimsValidator;

    @Autowired
    private JwtDecoderService jwtDecoderService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @BeforeEach
    void prepare() {
        restTemplate = new TestRestTemplate(new RestTemplateBuilder().redirects(HttpRedirects.DONT_FOLLOW));
        userRepository.findAll().forEach(user -> userService.deleteById(user.id()));
    }

    @AfterEach
    void cleanUp() {
        userRepository.findAll().forEach(user -> userService.deleteById(user.id()));
    }

    @Test
    void happyPath() {
        // given.
        String username = "test-user";
        String firstName = "John";
        String lastName = "Doe";
        String email = "example@gmail.com";
        String jobTitle = "Software Engineer";
        String organizationalUnit = "Engineering";
        Map<String, Object> identityClaims =
                Map.of("employment", Map.of("jobTitle", jobTitle, "organizationalUnit", organizationalUnit));
        // language=json
        String userInfoJson = """
                {
                  "given_name": "%s",
                  "family_name": "%s",
                  "email": "%s"
                }
                """.formatted(firstName, lastName, email);

        // and.
        when(oidcClient.exchangeCodeForTokens(CODE)).thenReturn(tokens);
        when(oidcClient.validateIdToken(ID_TOKEN)).thenReturn(new ValidatedOidcIdentity(username, identityClaims));
        when(oidcClient.validateAccessTokenAndExtractUserInfo(ACCESS_TOKEN)).thenReturn(userInfoJson);
        when(userInfoJsonAccessor.extractRole(userInfoJson)).thenReturn(TestRoles.EDITOR);
        when(userInfoJsonAccessor.extractTextField(userInfoJson, "given_name")).thenReturn(firstName);
        when(userInfoJsonAccessor.extractTextField(userInfoJson, "family_name")).thenReturn(lastName);
        when(userInfoJsonAccessor.extractTextField(userInfoJson, "email")).thenReturn(email);
        when(userInfoJsonAccessor.extractTextField(userInfoJson, "employment.jobTitle"))
                .thenReturn(jobTitle);
        when(userInfoJsonAccessor.extractTextField(userInfoJson, "employment.organizationalUnit"))
                .thenReturn(organizationalUnit);
        Instant beforeLogin = Instant.now();

        // when.
        ResponseEntity<Void> response = restTemplate.getForEntity(
                "http://localhost:" + port + "/api/external/oauth2/callback?code=" + CODE, Void.class);

        Instant afterLogin = Instant.now();

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
        assertThat(decodedTokenToUser.getRoles()).singleElement().satisfies(role -> {
            assertThat(role.getName()).isEqualTo(TestRoles.EDITOR.getName());
            assertThat(role.getAuthorities())
                    .extracting(Authority::getName)
                    .containsExactlyInAnyOrderElementsOf(TestRoles.EDITOR.getAuthorities().stream()
                            .map(Authority::getName)
                            .toList());
        });

        String decodedAuthorities = assertThat(
                        // trying to extract an actual token from the cookie value
                        authoritiesCookie.substring(authoritiesCookie.indexOf("=") + 1, authoritiesCookie.indexOf(";")))
                .isBase64()
                .asBase64Decoded()
                .asString()
                .actual();

        TestRoles.EDITOR.getAuthorities().stream()
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
        assertThat(userEntity.firstName()).isEqualTo(firstName);
        assertThat(userEntity.lastName()).isEqualTo(lastName);
        assertThat(userEntity.email()).isEqualTo(email);
        assertThat(userEntity.jobTitle()).isEqualTo(jobTitle);
        assertThat(userEntity.organizationalUnit()).isEqualTo(organizationalUnit);
        assertThat(userEntity.password()).isNull();
        assertThat(userService.findRoleNamesByUserId(userEntity.id()))
                .hasSize(1)
                .containsOnly("EDITOR");
        assertThat(userEntity.userOrigin()).isEqualTo(UserOrigin.OIDC);
        assertThat(userEntity.status()).isEqualTo(UserStatus.ACTIVE);
        assertThat(userEntity.lastLoginAt()).isNotNull().isBetween(beforeLogin, afterLogin);
        assertSuccessfulCallback(MasterWebEndpoints.OIDC_AUTH_COMPLETE, decodedTokenToUser);
    }

    @Test
    void shouldUpdateLastLoginAtForExistingOidcUser() {
        // given.
        String username = "test-user";
        String updatedEmail = "updated@gmail.com";
        userService.createFromOidc(
                username, "Original", "Name", "original@gmail.com", null, null, TestRoles.VIEWER.getName());

        // and.
        String userInfoJson = "{\"email\": \"%s\"}".formatted(updatedEmail);
        when(oidcClient.exchangeCodeForTokens(CODE)).thenReturn(tokens);
        when(oidcClient.validateIdToken(ID_TOKEN)).thenReturn(new ValidatedOidcIdentity(username, Map.of()));
        when(oidcClient.validateAccessTokenAndExtractUserInfo(ACCESS_TOKEN)).thenReturn(userInfoJson);
        when(userInfoJsonAccessor.extractRole(userInfoJson)).thenReturn(TestRoles.EDITOR);
        when(userInfoJsonAccessor.extractTextField(userInfoJson, "email")).thenReturn(updatedEmail);
        Instant beforeLogin = Instant.now();

        // when.
        ResponseEntity<Void> response = restTemplate.getForEntity(
                "http://localhost:" + port + "/api/external/oauth2/callback?code=" + CODE, Void.class);
        Instant afterLogin = Instant.now();

        // then.
        UserEntity updated = userRepository.findByUsername(username).orElseThrow();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FOUND);
        assertThat(updated.firstName()).isEqualTo("Original");
        assertThat(updated.lastName()).isEqualTo("Name");
        assertThat(updated.email()).isEqualTo(updatedEmail);
        assertThat(userService.findRoleNamesByUserId(updated.id())).containsOnly(TestRoles.EDITOR.getName());
        assertThat(updated.userOrigin()).isEqualTo(UserOrigin.OIDC);
        assertThat(updated.lastLoginAt()).isNotNull().isBetween(beforeLogin, afterLogin);
        assertSuccessfulCallback(MasterWebEndpoints.OIDC_AUTH_COMPLETE, new PasswordlessUser(username, Set.of()));
    }

    @Test
    void shouldReturnForbiddenForSuspendedOidcUserWithoutUpdatingIt() {
        // given.
        String username = "test-user";
        userService.createFromOidc(
                username, "Original", "Name", "original@gmail.com", null, null, TestRoles.VIEWER.getName());
        UserEntity created = userRepository.findByUsername(username).orElseThrow();
        userService.updateStatus(created.id(), UserStatus.SUSPENDED);
        UserEntity suspended = userRepository.findById(created.id()).orElseThrow();

        // and.
        String userInfoJson = "{\"email\": \"updated@gmail.com\"}";
        when(oidcClient.exchangeCodeForTokens(CODE)).thenReturn(tokens);
        when(oidcClient.validateIdToken(ID_TOKEN)).thenReturn(new ValidatedOidcIdentity(username, Map.of()));
        when(oidcClient.validateAccessTokenAndExtractUserInfo(ACCESS_TOKEN)).thenReturn(userInfoJson);
        when(userInfoJsonAccessor.extractRole(userInfoJson)).thenReturn(TestRoles.EDITOR);

        // when.
        ResponseEntity<String> response = restTemplate.getForEntity(
                "http://localhost:" + port + "/api/external/oauth2/callback?code=" + CODE, String.class);

        // then.
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody()).contains("USER_SUSPENDED");
        assertThat(response.getHeaders().get(HttpHeaders.SET_COOKIE)).isNullOrEmpty();
        assertThat(userRepository.findById(created.id()).orElseThrow()).isEqualTo(suspended);
        assertAccessDenied(MasterWebEndpoints.OIDC_AUTH_COMPLETE);
    }

    @Test
    void shouldReturn400WhenOidcUserConflictsWithExistingLocalAccount() {
        // given.
        String username = "test-user";
        userService.createLocal(username, null, null, null, null, null, "test-password", "ADMIN");

        // and.
        String userInfoJson = "someJson";
        when(oidcClient.exchangeCodeForTokens(CODE)).thenReturn(tokens);
        when(oidcClient.validateIdToken(ID_TOKEN)).thenReturn(new ValidatedOidcIdentity(username, Map.of()));
        when(oidcClient.validateAccessTokenAndExtractUserInfo(ACCESS_TOKEN)).thenReturn(userInfoJson);
        when(userInfoJsonAccessor.extractRole(userInfoJson)).thenReturn(TestRoles.EDITOR);

        // when.
        ResponseEntity<Void> response = restTemplate.getForEntity(
                "http://localhost:" + port + "/api/external/oauth2/callback?code=" + CODE, Void.class);

        Optional<UserEntity> userInDb = userRepository.findByUsername(username);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(userInDb).isPresent().hasValueSatisfying(userEntity -> {
            assertThat(userEntity.userOrigin()).isEqualTo(UserOrigin.LOCAL);
            assertThat(userService.findRoleNamesByUserId(userEntity.id())).containsOnly("ADMIN");
        });
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
        when(oidcClient.validateIdToken(ID_TOKEN)).thenThrow(new InvalidJwtTokenException("invalid token"));

        // when.
        ResponseEntity<Void> response = restTemplate.getForEntity(
                "http://localhost:" + port + "/api/external/oauth2/callback?code=" + CODE, Void.class);

        // then.
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void shouldReturn401AndSkipUserProvisioningWhenClaimsValidatorRejects() {
        // given.
        String username = "test-user";
        when(oidcClient.exchangeCodeForTokens(CODE)).thenReturn(tokens);
        when(oidcClient.validateIdToken(ID_TOKEN)).thenReturn(new ValidatedOidcIdentity(username, Map.of()));
        doThrow(new OAuth2AuthenticationException("claims validation failed"))
                .when(claimsValidator)
                .validate(Map.of());

        // when.
        ResponseEntity<Void> response = restTemplate.getForEntity(
                "http://localhost:" + port + "/api/external/oauth2/callback?code=" + CODE, Void.class);

        // then.
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getHeaders().get(HttpHeaders.SET_COOKIE)).isNullOrEmpty();
        assertThat(userRepository.findByUsername(username)).isEmpty();
    }

    @Test
    void shouldReturn502WhenUserInfoEndpointUnavailable() {
        when(oidcClient.exchangeCodeForTokens(CODE)).thenReturn(tokens);
        when(oidcClient.validateIdToken(ID_TOKEN)).thenReturn(new ValidatedOidcIdentity("test-user", Map.of()));
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

    @Override
    protected Set<TestableMasterWebEndpoint> endpointsUnderTest() {
        return Set.of(); // No bad auth tests for oauth2 in the parent - all in this source file
    }
}
