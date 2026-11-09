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
package com.axelixlabs.axelix.master.service.auth;

import java.time.Duration;
import java.util.Set;

import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.axelixlabs.axelix.common.auth.core.JwtAlgorithm;
import com.axelixlabs.axelix.common.auth.core.TechUser;
import com.axelixlabs.axelix.common.auth.service.HmacVerificationStrategy;
import com.axelixlabs.axelix.common.auth.service.tech.DefaultTechUserJwtDeserializer;
import com.axelixlabs.axelix.common.auth.service.tech.DefaultTechUserJwtSerializer;
import com.axelixlabs.axelix.master.domain.auth.DefaultSecurityContext;
import com.axelixlabs.axelix.master.domain.auth.PasswordlessUser;
import com.axelixlabs.axelix.master.service.auth.jwt.DefaultUserJwtDecoderService;
import com.axelixlabs.axelix.master.service.auth.jwt.DefaultUserJwtEncoderService;
import com.axelixlabs.axelix.master.testfixtures.TestRoles;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link StarterAuthenticationTokenProvider}.
 *
 * @author Mikhail Polivakha
 */
@SpringBootTest
class StarterAuthenticationTokenProviderTest {

    @Autowired
    protected TestRoles testRoles;

    private static final String SIGNING_KEY =
            "22573444698685aa77750b88ad6e99ef1f94a7a909bc7df63f7a80666208c201ab7a584e6e05e6c9d0aa94b723f843ff";
    private final ScopedValueSecurityContextExecutor securityContextExecutor = new ScopedValueSecurityContextExecutor();
    private final DefaultUserJwtEncoderService encoder =
            new DefaultUserJwtEncoderService(JwtAlgorithm.HMAC512, SIGNING_KEY, Duration.ofMinutes(5));
    private final StarterAuthenticationTokenProvider tokenProvider = new StarterAuthenticationTokenProvider(
            securityContextExecutor, encoder, new DefaultTechUserJwtSerializer(JwtAlgorithm.HMAC512, SIGNING_KEY));

    @ParameterizedTest
    @ValueSource(strings = {"1.1.0", "1.1.1", "1.2.0", "1.2.1", "1.2.2"})
    void shouldPreserveCallerIdentityAndAuthoritiesForLegacyStarters(String version) {
        // given.
        PasswordlessUser caller = new PasswordlessUser("user-id", "operator", Set.of(testRoles.editor()));
        String callerToken = encoder.generateToken(caller);

        // when.
        String token = securityContextExecutor.callWithinSecurityContext(
                () -> tokenProvider.tokenFor(version), new DefaultSecurityContext(callerToken));

        // then.
        assertThat(token).isEqualTo(callerToken);
        PasswordlessUser decoded = new DefaultUserJwtDecoderService(
                        authoritiesService(), JwtAlgorithm.HMAC512, SIGNING_KEY)
                .decodeTokenToUser(token);
        assertThat(decoded.getId()).isEqualTo(caller.getId());
        assertThat(decoded.getRoles())
                .flatExtracting(role -> role.getEffectiveAuthorities())
                .containsExactlyInAnyOrderElementsOf(testRoles.editor().authorities());
    }

    @ParameterizedTest
    @ValueSource(strings = {"1.3.0"})
    void shouldUseMasterIdentityWithoutCallerContextForModernStarters(String version) {
        // when.
        String token = tokenProvider.tokenFor(version);

        // then.
        assertThat(new DefaultTechUserJwtDeserializer(JwtAlgorithm.HMAC512, SIGNING_KEY).deserialize(token))
                .isEqualTo(TechUser.MASTER);
        assertThat(new HmacVerificationStrategy()
                        .verifyAndParse(token, SIGNING_KEY)
                        .getPayload())
                .containsOnlyKeys("sub", "iat", "exp");
    }

    @ParameterizedTest
    @ValueSource(strings = {"1.1.0", "1.2.0"})
    void shouldRequireCallerContextForLegacyStarters(String version) {
        // when / then.
        assertThatThrownBy(() -> tokenProvider.tokenFor(version)).isInstanceOf(IllegalStateException.class);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"unknown", " ", "1.3"})
    void shouldRejectUnknownVersions(String version) {
        // when / then.
        assertThatThrownBy(() -> tokenProvider.tokenFor(version)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldUseDiscoveryTokenAcceptedByBothProtocolGenerations() {
        // when.
        String token = tokenProvider.discoveryToken();

        // then.
        PasswordlessUser legacyUser = new DefaultUserJwtDecoderService(
                        authoritiesService(), JwtAlgorithm.HMAC512, SIGNING_KEY)
                .decodeTokenToUser(token);
        assertThat(legacyUser.getId()).isEqualTo(TechUser.MASTER.getId());
        assertThat(legacyUser.getUsername()).isEqualTo(TechUser.MASTER.getId());
        assertThat(legacyUser.getRoles()).isEmpty();
        Claims claims = new HmacVerificationStrategy()
                .verifyAndParse(token, SIGNING_KEY)
                .getPayload();
        assertThat(Duration.between(
                        claims.getIssuedAt().toInstant(), claims.getExpiration().toInstant()))
                .isEqualTo(Duration.ofMinutes(5));
        assertThat(new DefaultTechUserJwtDeserializer(JwtAlgorithm.HMAC512, SIGNING_KEY).deserialize(token))
                .isEqualTo(TechUser.MASTER);
    }

    private AuthoritiesService authoritiesService() {
        AuthoritiesService service = mock(AuthoritiesService.class);
        when(service.getAll()).thenReturn(testRoles.editor().authorities());
        return service;
    }
}
