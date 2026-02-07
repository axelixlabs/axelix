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
package com.axelixlabs.axelix.common.auth;

import java.util.Set;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;

import com.axelixlabs.axelix.common.auth.core.DecodedUser;
import com.axelixlabs.axelix.common.auth.core.ExternalAuthority;
import com.axelixlabs.axelix.common.auth.core.DefaultRole;
import com.axelixlabs.axelix.common.auth.core.JwtAlgorithm;
import com.axelixlabs.axelix.common.auth.core.Role;
import com.axelixlabs.axelix.common.auth.exception.ExpiredJwtTokenException;
import com.axelixlabs.axelix.common.auth.exception.InvalidJwtTokenException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Integration tests for {@link JwtDecoderService}, verifying correct decoding and validation of JWT tokens.
 *
 * @author Nikita Kirillov
 * @since 22.07.2025
 */
@SpringBootTest(classes = DefaultJwtDecoderServiceTest.JwtDecoderServiceConfig.class)
class DefaultJwtDecoderServiceTest {

    @Autowired
    private JwtDecoderService jwtDecoderService;

    @Value("${test-tokens.token-user-with-two-role}")
    private String tokenUserWithTwoRole;

    @Value("${test-tokens.token-user-with-admin-role-hierarchy}")
    private String tokenUserWithAdminRoleHierarchy;

    @Value("${test-tokens.token-with-hs256-algorithm}")
    private String tokenWithHs256Algorithm;

    @Value("${test-tokens.token-with-hs384-algorithm}")
    private String tokenWithHs384Algorithm;

    @Value("${test-tokens.token-with-empty-roles}")
    private String tokenWithEmptyRoles;

    @Value("${test-tokens.expired-token}")
    private String expiredToken;

    @Value("${test-tokens.token-signed-with-wrong-key}")
    private String tokenSignedWithWrongKey;

    @Value("${test-tokens.token-user-with-unrecognized-authorities}")
    private String tokenUserWithUnrecognizedAuthorities;

    @Test
    void shouldDecodeValidJwtToken() {
        DecodedUser decodedUser = jwtDecoderService.decodeTokenToUser(tokenUserWithTwoRole);

        Role userRole = decodedUser.getRoles().stream()
                .filter(role -> role.getName().equals("ROLE_USER"))
                .findFirst()
                .orElseThrow();

        Role engineerRole = decodedUser.getRoles().stream()
                .filter(role -> role.getName().equals("ROLE_ENGINEER"))
                .findFirst()
                .orElseThrow();

        assertThat(userRole.getAuthorities()).containsAll(Set.of(ExternalAuthority.ENV, ExternalAuthority.INFO));
        assertThat(engineerRole.getAuthorities()).containsAll(Set.of(ExternalAuthority.BEANS, ExternalAuthority.HEALTH));
    }

    @Test
    void shouldDecodeValidJwtToken_WithRoleHierarchy() {
        DecodedUser decodedUser = jwtDecoderService.decodeTokenToUser(tokenUserWithAdminRoleHierarchy);

        Role rootRole = decodedUser.getRoles().stream()
                .filter(role -> role.getName().equals("ROLE_ROOT"))
                .findFirst()
                .orElseThrow();

        assertThat(rootRole.getComponents()).hasSize(2);

        Role adminRole = rootRole.getComponents().stream()
                .filter(role -> role.getName().equals("ROLE_ADMIN"))
                .findFirst()
                .orElseThrow();

        assertThat(adminRole.getAuthorities()).contains(ExternalAuthority.PROFILE_MANAGEMENT);

        Role engineerRole = adminRole.getComponents().iterator().next();

        assertThat(engineerRole.getName()).isEqualTo("ROLE_ENGINEER");
        assertThat(engineerRole.getAuthorities()).isEqualTo(Set.of(ExternalAuthority.ENV));

        Role userRole = engineerRole.getComponents().iterator().next();

        assertThat(userRole.getName()).isEqualTo("ROLE_USER");
        assertThat(userRole.getAuthorities()).isEqualTo(Set.of(ExternalAuthority.INFO));

        Role readRole = rootRole.getComponents().stream()
                .filter(role -> role.getName().equals("ROLE_READ"))
                .findFirst()
                .orElseThrow();

        assertThat(readRole.getAuthorities()).contains(ExternalAuthority.BEANS);
    }

    @Test
    void shouldEncodeDecodeTokenWithHS256() {
        String key256 = "79912c6adb2a4f6c78a859807b072ce2a2c1140ac578f324cca983db22868b14";
        JwtDecoderService decoder256 = new DefaultJwtDecoderService(JwtAlgorithm.HMAC256, key256);

        DecodedUser expectedUser = new DecodedUser(
                "testUser", Set.of(new DefaultRole("ROLE_USER", Set.of(ExternalAuthority.MAPPINGS), Set.of())));

        DecodedUser decodedUser = decoder256.decodeTokenToUser(tokenWithHs256Algorithm);

        assertThat(decodedUser).usingRecursiveComparison().isEqualTo(expectedUser);
    }

    @Test
    void shouldEncodeDecodeTokenWithHS384() {
        String key384 =
                "bfa30eb1f16c07ba0a6a19a60f7c4bc02e1e10670411ae7a2f206b2bfe8801e2bb40741469d95fbbf4c86ae4b4a68437";
        JwtDecoderService decoder384 = new DefaultJwtDecoderService(JwtAlgorithm.HMAC384, key384);

        DecodedUser expectedUser = new DecodedUser(
                "testUser", Set.of(new DefaultRole("ROLE_USER", Set.of(ExternalAuthority.BEANS), Set.of())));

        DecodedUser decodedUser = decoder384.decodeTokenToUser(tokenWithHs384Algorithm);

        assertThat(decodedUser).usingRecursiveComparison().isEqualTo(expectedUser);
    }

    @Test
    void shouldOmitInvalidAuthority() {
        DecodedUser decodedUser = jwtDecoderService.decodeTokenToUser(tokenUserWithUnrecognizedAuthorities);

        assertThat(decodedUser.getRoles())
                .first()
                .satisfies(role -> assertThat(role.getAuthorities()).hasSize(1).containsOnly(ExternalAuthority.ENV));
    }

    @Test
    void shouldDecodeValidJwtTokenWithoutUserRoles() {
        DecodedUser decodedUser = jwtDecoderService.decodeTokenToUser(tokenWithEmptyRoles);

        assertThat(decodedUser.getUsername()).isEqualTo("userWithEmptyRoles");
        assertThat(decodedUser.getRoles()).isEmpty();
    }

    @Test
    void shouldThrowOnExpiredToken() {
        assertThatThrownBy(() -> jwtDecoderService.decodeTokenToUser(expiredToken))
                .isInstanceOf(ExpiredJwtTokenException.class)
                .hasMessageStartingWith("JWT token has expired");
    }

    @Test
    void shouldThrowOnTamperedToken() {
        String tamperedToken = tokenUserWithAdminRoleHierarchy + "x";

        assertThatThrownBy(() -> jwtDecoderService.decodeTokenToUser(tamperedToken))
                .isInstanceOf(InvalidJwtTokenException.class)
                .hasMessage("JWT token is invalid or tampered");
    }

    @Test
    void shouldFailToDecodeTokenWithWrongSecret() {
        assertThatThrownBy(() -> jwtDecoderService.decodeTokenToUser(tokenSignedWithWrongKey))
                .isInstanceOf(InvalidJwtTokenException.class)
                .hasMessage("JWT token is invalid or tampered");
    }

    /**
     * Minimal test configuration for {@link JwtDecoderService} integration testing.
     *
     * <p>Registers beans for {@link JwtDecoderService}, allowing
     * full-stack testing of JWT encoding and decoding within a Spring context.</p>
     */
    @SpringBootConfiguration
    public static class JwtDecoderServiceConfig {

        @Bean
        public JwtDecoderService jwtDecoderService(
                final @Value("${axelix.master.auth.jwt.algorithm}") JwtAlgorithm algorithm,
                final @Value("${axelix.master.auth.jwt.signing-key}") String signingKey) {
            return new DefaultJwtDecoderService(algorithm, signingKey);
        }
    }
}
