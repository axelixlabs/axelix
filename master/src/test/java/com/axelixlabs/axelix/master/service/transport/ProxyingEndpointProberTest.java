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
package com.axelixlabs.axelix.master.service.transport;

import java.io.IOException;
import java.net.http.HttpClient;
import java.util.List;
import java.util.Set;

import com.axelixlabs.axelix.master.service.auth.jwt.UserJwtDecoderService;
import io.jsonwebtoken.Claims;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import tools.jackson.databind.ObjectMapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.axelixlabs.axelix.common.auth.core.TechUser;
import com.axelixlabs.axelix.common.auth.service.HmacVerificationStrategy;
import com.axelixlabs.axelix.common.contract.metadata.BasicRegistrationMetadata;
import com.axelixlabs.axelix.common.domain.http.DefaultHttpPayload;
import com.axelixlabs.axelix.common.domain.http.HttpHeader;
import com.axelixlabs.axelix.master.autoconfiguration.auth.properties.JwtProperties;
import com.axelixlabs.axelix.master.domain.ActuatorEndpoints;
import com.axelixlabs.axelix.master.domain.auth.DefaultSecurityContext;
import com.axelixlabs.axelix.master.domain.auth.PasswordlessUser;
import com.axelixlabs.axelix.master.domain.auth.SecurityContextExecutor;
import com.axelixlabs.axelix.master.domain.http.NoHttpPayload;
import com.axelixlabs.axelix.master.domain.instance.Instance;
import com.axelixlabs.axelix.master.service.auth.jwt.UserJwtEncoderService;
import com.axelixlabs.axelix.master.service.auth.StarterAuthenticationTokenProvider;
import com.axelixlabs.axelix.master.service.serde.DefaultJacksonMessageDeserializationStrategy;
import com.axelixlabs.axelix.master.service.state.InstanceRegistry;
import com.axelixlabs.axelix.master.testfixtures.TestRoles;
import com.axelixlabs.axelix.master.utils.TestInstanceFactory;
import com.axelixlabs.axelix.master.utils.TestMetadataFactory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Integration tests for {@link ProxyingEndpointProber}.
 *
 * @author Mikhail Polivakha
 */
@SpringBootTest
@Transactional
class ProxyingEndpointProberTest {

    @Autowired
    private SecurityContextExecutor securityContextExecutor;

    @Autowired
    private UserJwtEncoderService encoder;

    @Autowired
    private UserJwtDecoderService decoder;

    @Autowired
    private JwtProperties jwtProperties;

    @Autowired
    private InstanceRegistry registry;

    private MockWebServer server;

    @Autowired
    private StarterAuthenticationTokenProvider tokenProvider;

    @Autowired
    private HttpClient httpClient;

    private EndpointProber<byte[]> endEndpointProber;

    private EndpointProber<BasicRegistrationMetadata> metadataProber;

    @BeforeEach
    void setUp() throws IOException {
        server = new MockWebServer();
        server.start();
        endEndpointProber = new ProxyingEndpointProber(
                registry, ActuatorEndpoints.GET_ALL_ENV_PROPERTIES, tokenProvider, httpClient);
        metadataProber = new DefaultEndpointProber<>(
                registry,
                new DefaultJacksonMessageDeserializationStrategy<>(new ObjectMapper(), BasicRegistrationMetadata.class),
                tokenProvider,
                ActuatorEndpoints.METADATA,
                httpClient);
    }

    @AfterEach
    void tearDown() throws IOException {
        server.shutdown();
    }

    @ParameterizedTest
    @ValueSource(strings = {"1.1.0", "1.1.1", "1.2.0", "1.2.1", "1.2.2"})
    void shouldForwardCallerTokenToRegisteredLegacyStarter(String version) throws InterruptedException {
        // given.
        Instance instance = instance(version);
        server.enqueue(new MockResponse().setBody("legacy-response"));
        PasswordlessUser caller = new PasswordlessUser("caller", "operator", Set.of(TestRoles.EDITOR));
        String callerToken = encoder.generateToken(caller);

        // when.
        byte[] response = securityContextExecutor.callWithinSecurityContext(
                () -> endEndpointProber.invoke(instance.id(), NoHttpPayload.INSTANCE),
                new DefaultSecurityContext(callerToken));

        // then.
        RecordedRequest request = server.takeRequest();
        assertThat(request.getPath()).isEqualTo("/actuator/axelix-env");
        assertThat(request.getHeader("Authorization")).isEqualTo("Bearer " + callerToken);
        assertThat(new String(response, java.nio.charset.StandardCharsets.UTF_8))
                .isEqualTo("legacy-response");
    }

    @ParameterizedTest
    @ValueSource(strings = {"1.3.0", "1.3.1"})
    void shouldReplaceForwardedAuthorizationForModernStarter(String version) throws InterruptedException {
        // given.
        Instance instance = instance(version);
        server.enqueue(new MockResponse().setBody("response"));
        DefaultHttpPayload payload =
                new DefaultHttpPayload(List.of(new HttpHeader("Authorization", "Bearer human-token")));

        // when.
        endEndpointProber.invoke(instance.id(), payload);

        // then.
        RecordedRequest request = server.takeRequest();
        assertThat(request.getHeaders().values("Authorization")).hasSize(1);
        String token = request.getHeader("Authorization").substring("Bearer ".length());
        assertThat(claims(token).getSubject()).isEqualTo(TechUser.MASTER.getId());
        assertThat(claims(token)).containsOnlyKeys("sub", "iat", "exp");
        assertThat(java.time.Duration.between(
                        claims(token).getIssuedAt().toInstant(),
                        claims(token).getExpiration().toInstant()))
                .isEqualTo(jwtProperties.lifespan());
    }

    @Test
    void shouldAuthenticateInitialMetadataDiscoveryWithoutCaller() throws InterruptedException {
        // given.
        BasicRegistrationMetadata metadata = TestMetadataFactory.create("com.example", "test-app");
        server.enqueue(new MockResponse().setBody(TestMetadataFactory.toJson(metadata)));

        // when.
        BasicRegistrationMetadata result =
                metadataProber.invoke(server.url("/actuator").toString(), NoHttpPayload.INSTANCE);

        // then.
        RecordedRequest request = server.takeRequest();
        assertThat(result).isEqualTo(metadata);
        assertThat(request.getPath()).isEqualTo("/actuator/axelix-metadata");
        String token = request.getHeader("Authorization").substring("Bearer ".length());
        assertThat(decoder.decodeTokenToUser(token).getUsername()).isEqualTo(TechUser.MASTER.getId());
        assertThat(claims(token).getSubject()).isEqualTo(TechUser.MASTER.getId());
    }

    @Test
    void shouldSelectAuthenticationSeparatelyForMixedStarterVersions() throws InterruptedException {
        // given.
        Instance legacy = instance("1.2.0");
        Instance modern = instance("1.3.0-SNAPSHOT");
        server.enqueue(new MockResponse().setBody("legacy"));
        server.enqueue(new MockResponse().setBody("modern"));
        PasswordlessUser caller = new PasswordlessUser("caller", "operator", Set.of(TestRoles.EDITOR));
        String callerToken = encoder.generateToken(caller);

        // when.
        securityContextExecutor.runWithinSecurityContext(
                () -> {
                    endEndpointProber.invoke(legacy.id(), NoHttpPayload.INSTANCE);
                    endEndpointProber.invoke(modern.id(), NoHttpPayload.INSTANCE);
                },
                new DefaultSecurityContext(callerToken));

        // then.
        assertThat(server.takeRequest().getHeader("Authorization")).isEqualTo("Bearer " + callerToken);
        String modernToken = server.takeRequest().getHeader("Authorization").substring("Bearer ".length());
        assertThat(claims(modernToken)).containsOnlyKeys("sub", "iat", "exp");
        assertThat(claims(modernToken).getSubject()).isEqualTo(TechUser.MASTER.getId());
    }

    @Test
    void shouldPropagateAuthenticationRejectionWithoutRetryingWithLegacyCredentials() {
        // given.
        Instance modern = instance("1.3.0");
        server.enqueue(new MockResponse().setResponseCode(401));

        // when / then.
        assertThatThrownBy(() -> endEndpointProber.invoke(modern.id(), NoHttpPayload.INSTANCE))
                .isInstanceOf(EndpointInvocationException.class);
        assertThat(server.getRequestCount()).isEqualTo(1);
    }

    private Claims claims(String token) {
        return new HmacVerificationStrategy()
                .verifyAndParse(token, jwtProperties.signingKey())
                .getPayload();
    }

    private Instance instance(String version) {
        Instance instance = TestInstanceFactory.withStarterVersion(
                "target-" + version, version, server.url("/actuator").toString());
        registry.register(instance);
        return instance;
    }
}
