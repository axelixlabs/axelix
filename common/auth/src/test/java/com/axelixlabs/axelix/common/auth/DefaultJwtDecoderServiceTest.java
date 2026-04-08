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

import java.time.Duration;
import java.util.Set;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

import com.axelixlabs.axelix.common.auth.core.Authority;
import com.axelixlabs.axelix.common.auth.core.DefaultAuthority;
import com.axelixlabs.axelix.common.auth.core.DefaultRole;
import com.axelixlabs.axelix.common.auth.core.DefaultUser;
import com.axelixlabs.axelix.common.auth.core.JwtAlgorithm;
import com.axelixlabs.axelix.common.auth.core.PasswordlessUser;
import com.axelixlabs.axelix.common.auth.core.Role;
import com.axelixlabs.axelix.common.auth.core.User;
import com.axelixlabs.axelix.common.auth.exception.ExpiredJwtTokenException;
import com.axelixlabs.axelix.common.auth.exception.InvalidJwtTokenException;
import com.axelixlabs.axelix.common.auth.exception.JwtParsingException;
import com.axelixlabs.axelix.common.auth.service.DefaultJwtDecoderService;
import com.axelixlabs.axelix.common.auth.service.DefaultJwtEncoderService;
import com.axelixlabs.axelix.common.auth.service.JwtDecoderService;
import com.axelixlabs.axelix.common.auth.service.JwtEncoderService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Integration tests for {@link JwtDecoderService}, verifying correct decoding and validation of JWT tokens.
 *
 * @author Nikita Kirillov
 * @author Sergey Cherkasov
 */
@SpringBootTest
@Import(DefaultJwtDecoderServiceTest.JwtDecoderServiceConfig.class)
class DefaultJwtDecoderServiceTest {
    private static final String USER_NAME = "testUser";
    private static final String PASSWORD = "testPassword";

    @Value("${axelix.master.auth.jwt.signing_key}")
    private String signingKey;

    @Value("${axelix.master.auth.jwt.lifespan}")
    private Duration lifespan;

    @Autowired
    private JwtDecoderService jwtDecoderService;

    @Autowired
    private JwtEncoderService jwtEncoderService;

    @Test
    void shouldDecodeValidJwtToken_ForUserWithRoleAdmin() {
        User user = new DefaultUser(USER_NAME, PASSWORD, Set.of(DefaultRole.ADMIN));
        String token = jwtEncoderService.generateToken(user);

        // when.
        PasswordlessUser decodedUser = jwtDecoderService.decodeTokenToUser(token);

        // then.
        assertThat(decodedUser.getRoles().stream()
                        .filter(role -> role.getName().equals("ADMIN"))
                        .findFirst()
                        .orElseThrow()
                        .getAuthorities())
                .containsExactlyInAnyOrder(
                        DefaultAuthority.SCHEDULED_TASKS_MODIFY,
                        DefaultAuthority.CACHES_CLEAR,
                        DefaultAuthority.CACHES_TOGGLE,
                        DefaultAuthority.GARBAGE_COLLECTOR,
                        DefaultAuthority.CONFIG_PROPS_VALUES_READ,
                        DefaultAuthority.ENV_VALUES_READ);
    }

    @Test
    void shouldDecodeValidJwtToken_ForEditorWithRoleAdmin() {
        User user = new DefaultUser(USER_NAME, PASSWORD, Set.of(DefaultRole.EDITOR));
        String token = jwtEncoderService.generateToken(user);

        // when.
        PasswordlessUser decodedUser = jwtDecoderService.decodeTokenToUser(token);

        // then.
        assertThat(decodedUser.getRoles().stream()
                        .filter(role -> role.getName().equals("EDITOR"))
                        .findFirst()
                        .orElseThrow()
                        .getAuthorities())
                .containsExactlyInAnyOrder(
                        DefaultAuthority.SCHEDULED_TASKS_MODIFY,
                        DefaultAuthority.CACHES_CLEAR,
                        DefaultAuthority.CACHES_TOGGLE,
                        DefaultAuthority.GARBAGE_COLLECTOR);
    }

    @Test
    void shouldDecodeValidJwtToken_ForViewerWithRoleAdmin() {
        User user = new DefaultUser(USER_NAME, PASSWORD, Set.of(DefaultRole.VIEWER));
        String token = jwtEncoderService.generateToken(user);

        // when.
        PasswordlessUser decodedUser = jwtDecoderService.decodeTokenToUser(token);

        // then.
        assertThat(decodedUser.getRoles().stream()
                        .filter(role -> role.getName().equals("VIEWER"))
                        .findFirst()
                        .orElseThrow()
                        .getAuthorities())
                .isEmpty();
    }

    @Test
    void shouldDecodeValidJwtToken_MultipleRoles() {
        User user =
                new DefaultUser(USER_NAME, PASSWORD, Set.of(DefaultRole.ADMIN, DefaultRole.EDITOR, DefaultRole.VIEWER));
        String token = jwtEncoderService.generateToken(user);

        // when.
        PasswordlessUser decodedUser = jwtDecoderService.decodeTokenToUser(token);

        // Admin
        assertThat(decodedUser.getRoles().stream()
                        .filter(role -> role.getName().equals("ADMIN"))
                        .findFirst()
                        .orElseThrow()
                        .getAuthorities())
                .containsExactlyInAnyOrder(
                        DefaultAuthority.SCHEDULED_TASKS_MODIFY,
                        DefaultAuthority.CACHES_CLEAR,
                        DefaultAuthority.CACHES_TOGGLE,
                        DefaultAuthority.GARBAGE_COLLECTOR,
                        DefaultAuthority.CONFIG_PROPS_VALUES_READ,
                        DefaultAuthority.ENV_VALUES_READ);

        // Editor
        assertThat(decodedUser.getRoles().stream()
                        .filter(role -> role.getName().equals("EDITOR"))
                        .findFirst()
                        .orElseThrow()
                        .getAuthorities())
                .containsExactlyInAnyOrder(
                        DefaultAuthority.SCHEDULED_TASKS_MODIFY,
                        DefaultAuthority.CACHES_CLEAR,
                        DefaultAuthority.CACHES_TOGGLE,
                        DefaultAuthority.GARBAGE_COLLECTOR);

        // Viewer
        assertThat(decodedUser.getRoles().stream()
                        .filter(role -> role.getName().equals("VIEWER"))
                        .findFirst()
                        .orElseThrow()
                        .getAuthorities())
                .isEmpty();
    }

    @Test
    void shouldEncodeDecodeTokenWithHS256() {
        String key256 = "79912c6adb2a4f6c78a859807b072ce2a2c1140ac578f324cca983db22868b14";
        JwtEncoderService encoder = new DefaultJwtEncoderService(JwtAlgorithm.HMAC256, key256, lifespan);
        String token = encoder.generateToken(new DefaultUser(USER_NAME, PASSWORD, Set.of(DefaultRole.EDITOR)));
        JwtDecoderService decoder256 = new DefaultJwtDecoderService(JwtAlgorithm.HMAC256, key256);

        // when.
        PasswordlessUser decodedUser = decoder256.decodeTokenToUser(token);

        // then.
        PasswordlessUser expectedUser = new PasswordlessUser(USER_NAME, Set.of(DefaultRole.EDITOR));
        assertThat(decodedUser).usingRecursiveComparison().isEqualTo(expectedUser);
    }

    @Test
    void shouldEncodeDecodeTokenWithHS384() {
        String key384 =
                "bfa30eb1f16c07ba0a6a19a60f7c4bc02e1e10670411ae7a2f206b2bfe8801e2bb40741469d95fbbf4c86ae4b4a68437";
        JwtEncoderService encoder = new DefaultJwtEncoderService(JwtAlgorithm.HMAC384, key384, lifespan);
        String token = encoder.generateToken(new DefaultUser(USER_NAME, PASSWORD, Set.of(DefaultRole.ADMIN)));
        JwtDecoderService decoder384 = new DefaultJwtDecoderService(JwtAlgorithm.HMAC384, key384);

        // when.
        PasswordlessUser decodedUser = decoder384.decodeTokenToUser(token);

        // then.
        PasswordlessUser expectedUser = new PasswordlessUser(USER_NAME, Set.of(DefaultRole.ADMIN));
        assertThat(decodedUser).usingRecursiveComparison().isEqualTo(expectedUser);
    }

    @Test
    void shouldOmitInvalidAuthority() {
        Role role = new DefaultRole("VIEWER", Set.of(UnrecognizedAuthority.UNRECOGNIZED_AUTHORITY));
        User user = new DefaultUser(USER_NAME, PASSWORD, Set.of(role));
        String token = jwtEncoderService.generateToken(user);

        PasswordlessUser decodedUser = jwtDecoderService.decodeTokenToUser(token);

        assertThat(decodedUser.getRoles()).first().satisfies(r -> assertThat(r.getAuthorities())
                .hasSize(0));
    }

    @Test
    void shouldDecodeValidJwtTokenWithoutUserRoles() {
        User user = new DefaultUser(USER_NAME, PASSWORD, Set.of());
        String token = jwtEncoderService.generateToken(user);
        PasswordlessUser decodedUser = jwtDecoderService.decodeTokenToUser(token);

        assertThat(decodedUser.getUsername()).isEqualTo(USER_NAME);
        assertThat(decodedUser.getRoles()).isEmpty();
    }

    @Test
    void shouldThrowOnExpiredToken_TokenWithNullNameRoles() {
        Role role = new DefaultRole(null, Set.of(DefaultAuthority.CACHES_CLEAR));
        User user = new DefaultUser(USER_NAME, PASSWORD, Set.of(role));
        String token = jwtEncoderService.generateToken(user);

        assertThatThrownBy(() -> jwtDecoderService.decodeTokenToUser(token)).isInstanceOf(JwtParsingException.class);
    }

    @Test
    void shouldThrowOnExpiredToken() {
        User user = new DefaultUser(USER_NAME, PASSWORD, Set.of(DefaultRole.ADMIN));
        String token = jwtEncoderService.generateToken(user, Duration.ofDays(0));

        assertThatThrownBy(() -> jwtDecoderService.decodeTokenToUser(token))
                .isInstanceOf(ExpiredJwtTokenException.class);
    }

    @Test
    void shouldThrowOnTamperedToken() {
        User user = new DefaultUser(USER_NAME, PASSWORD, Set.of(DefaultRole.ADMIN));
        String token = jwtEncoderService.generateToken(user);

        assertThatThrownBy(() -> jwtDecoderService.decodeTokenToUser(token + "x"))
                .isInstanceOf(InvalidJwtTokenException.class);
    }

    @Test
    void shouldFailToDecodeTokenWithWrongSecret() {
        String wrongSecret = "MX3TNBx0j8bGCjGWCvq1JffIqqzXLIV-URlKFLX4mfA";
        JwtEncoderService encoderWithWrongSecret =
                new DefaultJwtEncoderService(JwtAlgorithm.HMAC256, wrongSecret, lifespan);

        User user = new DefaultUser(USER_NAME, PASSWORD, Set.of());
        String token = encoderWithWrongSecret.generateToken(user);

        assertThatThrownBy(() -> jwtDecoderService.decodeTokenToUser(token))
                .isInstanceOf(InvalidJwtTokenException.class);
    }

    enum UnrecognizedAuthority implements Authority {
        UNRECOGNIZED_AUTHORITY;

        @Override
        public String getName() {
            return name();
        }
    }

    /**
     * Minimal test configuration for {@link JwtDecoderService} integration testing.
     *
     * <p>Registers beans for {@link JwtDecoderService}, allowing
     * full-stack testing of JWT encoding and decoding within a Spring context.</p>
     */
    @SpringBootConfiguration
    static class JwtDecoderServiceConfig {

        @Bean
        public JwtDecoderService jwtDecoderService(
                final @Value("${axelix.master.auth.jwt.algorithm}") JwtAlgorithm algorithm,
                final @Value("${axelix.master.auth.jwt.signing_key}") String signingKey) {
            return new DefaultJwtDecoderService(algorithm, signingKey);
        }

        @Bean
        JwtEncoderService jwtEncoderService(
                final @Value("${axelix.master.auth.jwt.algorithm}") JwtAlgorithm algorithm,
                final @Value("${axelix.master.auth.jwt.lifespan}") Duration lifespan,
                final @Value("${axelix.master.auth.jwt.signing_key}") String signingKey) {
            return new DefaultJwtEncoderService(algorithm, signingKey, lifespan);
        }
    }
}
