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
import java.time.Duration;
import java.util.List;
import java.util.Set;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import com.axelixlabs.axelix.common.auth.core.DefaultSecurityContext;
import com.axelixlabs.axelix.common.auth.core.JwtAlgorithm;
import com.axelixlabs.axelix.common.auth.core.PasswordlessUser;
import com.axelixlabs.axelix.common.auth.core.TechUser;
import com.axelixlabs.axelix.common.auth.service.DefaultAuthoritiesManager;
import com.axelixlabs.axelix.common.auth.service.DefaultJwtDecoderService;
import com.axelixlabs.axelix.common.auth.service.DefaultJwtEncoderService;
import com.axelixlabs.axelix.common.auth.service.HmacVerificationStrategy;
import com.axelixlabs.axelix.common.auth.service.tech.DefaultTechUserJwtDeserializer;
import com.axelixlabs.axelix.common.auth.service.tech.DefaultTechUserJwtSerializer;
import com.axelixlabs.axelix.common.domain.ActuatorEndpoints;
import com.axelixlabs.axelix.common.domain.http.DefaultHttpPayload;
import com.axelixlabs.axelix.common.domain.http.HttpHeader;
import com.axelixlabs.axelix.common.domain.http.NoHttpPayload;
import com.axelixlabs.axelix.common.testfixtures.TestRoles;
import com.axelixlabs.axelix.master.domain.instance.Instance;
import com.axelixlabs.axelix.master.service.auth.ScopedValueSecurityContextExecutor;
import com.axelixlabs.axelix.master.service.auth.StarterAuthenticationTokenProvider;
import com.axelixlabs.axelix.master.service.state.InstanceRegistry;
import com.axelixlabs.axelix.master.utils.TestInstanceFactory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ProxyingEndpointProberTest {

    private static final String SIGNING_KEY =
            "22573444698685aa77750b88ad6e99ef1f94a7a909bc7df63f7a80666208c201ab7a584e6e05e6c9d0aa94b723f843ff";
    private final ScopedValueSecurityContextExecutor securityContextExecutor = new ScopedValueSecurityContextExecutor();
    private final DefaultJwtEncoderService encoder =
            new DefaultJwtEncoderService(JwtAlgorithm.HMAC512, SIGNING_KEY, Duration.ofMinutes(5));
    private final StarterAuthenticationTokenProvider tokenProvider = new StarterAuthenticationTokenProvider(
            securityContextExecutor, encoder, new DefaultTechUserJwtSerializer(JwtAlgorithm.HMAC512, SIGNING_KEY));

    private final InstanceRegistry registry = mock(InstanceRegistry.class);
    private MockWebServer server;
    private HttpClient httpClient;

    @BeforeEach
    void setUp() throws IOException {
        server = new MockWebServer();
        server.start();
        httpClient = HttpClient.newHttpClient();
    }

    @AfterEach
    void tearDown() throws IOException {
        httpClient.close();
        server.shutdown();
    }

    @ParameterizedTest
    @ValueSource(strings = {"1.1.0", "1.2.0", "1.2.99"})
    void shouldForwardCallerTokenToRegisteredLegacyStarter(String version) throws InterruptedException {
        // given.
        Instance instance = instance(version);
        server.enqueue(new MockResponse().setBody("legacy-response"));
        ProxyingEndpointProber prober =
                new ProxyingEndpointProber(registry, ActuatorEndpoints.GET_BEANS, tokenProvider, httpClient);
        PasswordlessUser caller = new PasswordlessUser("caller", "operator", Set.of(TestRoles.EDITOR));
        String callerToken = encoder.generateToken(caller);

        // when.
        byte[] response = securityContextExecutor.callWithinSecurityContext(
                () -> prober.invoke(instance.id(), NoHttpPayload.INSTANCE),
                new DefaultSecurityContext(caller, callerToken));

        // then.
        RecordedRequest request = server.takeRequest();
        assertThat(request.getPath()).isEqualTo("/actuator/axelix-beans");
        assertThat(request.getHeader("Authorization")).isEqualTo("Bearer " + callerToken);
        assertThat(new String(response, java.nio.charset.StandardCharsets.UTF_8))
                .isEqualTo("legacy-response");
    }

    @ParameterizedTest
    @ValueSource(strings = {"1.3.0-SNAPSHOT", "1.3.0", "1.4.0"})
    void shouldReplaceForwardedAuthorizationForModernStarter(String version) throws InterruptedException {
        // given.
        Instance instance = instance(version);
        server.enqueue(new MockResponse().setBody("response"));
        ProxyingEndpointProber prober =
                new ProxyingEndpointProber(registry, ActuatorEndpoints.GET_BEANS, tokenProvider, httpClient);
        DefaultHttpPayload payload =
                new DefaultHttpPayload(List.of(new HttpHeader("Authorization", "Bearer human-token")));

        // when.
        prober.invoke(instance.id(), payload);

        // then.
        RecordedRequest request = server.takeRequest();
        assertThat(request.getHeaders().values("Authorization")).hasSize(1);
        String token = request.getHeader("Authorization").substring("Bearer ".length());
        assertThat(new DefaultTechUserJwtDeserializer(JwtAlgorithm.HMAC512, SIGNING_KEY).deserialize(token))
                .isEqualTo(TechUser.MASTER);
        assertThat(new HmacVerificationStrategy()
                        .verifyAndParse(token, SIGNING_KEY)
                        .getPayload())
                .containsOnlyKeys("sub");
    }

    @Test
    void shouldAuthenticateInitialMetadataDiscoveryWithoutCaller() throws InterruptedException {
        // given.
        server.enqueue(new MockResponse().setBody("metadata"));
        DefaultEndpointProber<String> prober = new DefaultEndpointProber<>(
                registry,
                bytes -> new String(bytes, java.nio.charset.StandardCharsets.UTF_8),
                tokenProvider,
                ActuatorEndpoints.METADATA,
                httpClient);

        // when.
        String result = prober.invoke(server.url("/actuator").toString(), NoHttpPayload.INSTANCE);

        // then.
        RecordedRequest request = server.takeRequest();
        assertThat(result).isEqualTo("metadata");
        assertThat(request.getPath()).isEqualTo("/actuator/axelix-metadata");
        String token = request.getHeader("Authorization").substring("Bearer ".length());
        assertThat(new DefaultJwtDecoderService(new DefaultAuthoritiesManager(null), JwtAlgorithm.HMAC512, SIGNING_KEY)
                        .decodeTokenToUser(token)
                        .getUsername())
                .isEqualTo(TechUser.MASTER.getId());
        assertThat(new DefaultTechUserJwtDeserializer(JwtAlgorithm.HMAC512, SIGNING_KEY).deserialize(token))
                .isEqualTo(TechUser.MASTER);
    }

    @Test
    void shouldSelectAuthenticationSeparatelyForMixedStarterVersions() throws InterruptedException {
        // given.
        Instance legacy = instance("1.2.0");
        Instance modern = instance("1.3.0-SNAPSHOT");
        server.enqueue(new MockResponse().setBody("legacy"));
        server.enqueue(new MockResponse().setBody("modern"));
        ProxyingEndpointProber prober =
                new ProxyingEndpointProber(registry, ActuatorEndpoints.GET_BEANS, tokenProvider, httpClient);
        PasswordlessUser caller = new PasswordlessUser("caller", "operator", Set.of(TestRoles.EDITOR));
        String callerToken = encoder.generateToken(caller);

        // when.
        securityContextExecutor.runWithinSecurityContext(
                () -> {
                    prober.invoke(legacy.id(), NoHttpPayload.INSTANCE);
                    prober.invoke(modern.id(), NoHttpPayload.INSTANCE);
                },
                new DefaultSecurityContext(caller, callerToken));

        // then.
        assertThat(server.takeRequest().getHeader("Authorization")).isEqualTo("Bearer " + callerToken);
        String modernToken = server.takeRequest().getHeader("Authorization").substring("Bearer ".length());
        assertThat(new HmacVerificationStrategy()
                        .verifyAndParse(modernToken, SIGNING_KEY)
                        .getPayload())
                .containsOnlyKeys("sub");
        assertThat(new DefaultTechUserJwtDeserializer(JwtAlgorithm.HMAC512, SIGNING_KEY).deserialize(modernToken))
                .isEqualTo(TechUser.MASTER);
    }

    @Test
    void shouldPropagateAuthenticationRejectionWithoutRetryingWithLegacyCredentials() {
        // given.
        Instance modern = instance("1.3.0");
        server.enqueue(new MockResponse().setResponseCode(401));
        ProxyingEndpointProber prober =
                new ProxyingEndpointProber(registry, ActuatorEndpoints.GET_BEANS, tokenProvider, httpClient);

        // when / then.
        assertThatThrownBy(() -> prober.invoke(modern.id(), NoHttpPayload.INSTANCE))
                .isInstanceOf(EndpointInvocationException.class);
        assertThat(server.getRequestCount()).isEqualTo(1);
    }

    private Instance instance(String version) {
        Instance instance = TestInstanceFactory.withStarterVersion(
                "target-" + version, version, server.url("/actuator").toString());
        when(registry.getOrThrow(instance.id())).thenReturn(instance);
        return instance;
    }
}
