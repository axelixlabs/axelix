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
package com.axelixlabs.axelix.master.service.auth.jwt;

import java.time.Duration;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import com.axelixlabs.axelix.common.auth.core.JwtAlgorithm;
import com.axelixlabs.axelix.common.auth.exception.ExpiredJwtTokenException;
import com.axelixlabs.axelix.common.auth.exception.InvalidJwtTokenException;
import com.axelixlabs.axelix.common.auth.exception.JwtParsingException;
import com.axelixlabs.axelix.master.Main;
import com.axelixlabs.axelix.master.domain.auth.Authority;
import com.axelixlabs.axelix.master.domain.auth.DefaultRole;
import com.axelixlabs.axelix.master.domain.auth.DefaultUser;
import com.axelixlabs.axelix.master.domain.auth.PasswordlessUser;
import com.axelixlabs.axelix.master.domain.auth.Role;
import com.axelixlabs.axelix.master.domain.auth.TokenClaim;
import com.axelixlabs.axelix.master.domain.auth.User;
import com.axelixlabs.axelix.master.repository.AuthorityRepository;
import com.axelixlabs.axelix.master.service.auth.AuthoritiesService;
import com.axelixlabs.axelix.master.service.auth.DefaultAuthoritiesService;
import com.axelixlabs.axelix.master.testfixtures.TestAuthorities;
import com.axelixlabs.axelix.master.testfixtures.TestRoles;
import com.axelixlabs.axelix.master.testfixtures.UserUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Integration tests for {@link UserJwtDecoderService}, verifying correct decoding and validation of JWT tokens.
 *
 * @author Nikita Kirillov
 * @author Sergey Cherkasov
 * @author Mikhail Polivakha
 */
@SpringBootTest(classes = Main.class)
@Transactional
@TestPropertySource("classpath:jwt-services-test.properties")
class DefaultUserJwtDecoderServiceTest {

    private static final String USER_NAME = "testUser";
    private static final String PASSWORD = "testPassword";

    @Value("${axelix.master.auth.jwt.lifespan}")
    private Duration lifespan;

    @Value("${axelix.master.auth.jwt.signing_key}")
    private String signingKey;

    @Value("${axelix.master.auth.jwt.algorithm}")
    private JwtAlgorithm algorithm;

    @Autowired
    private UserJwtDecoderService userJwtDecoderService;

    @Autowired
    private UserJwtEncoderService userJwtEncoderService;

    @Autowired
    private AuthoritiesService authoritiesService;

    @Autowired
    private JdbcClient jdbcClient;

    @ParameterizedTest
    @MethodSource("roles")
    void shouldDecodeValidJwtToken_ForViewer(Role role) {
        // given.
        User user = UserUtils.withPassword(USER_NAME, PASSWORD, Set.of(role));
        String token = userJwtEncoderService.generateToken(user);

        // when.
        PasswordlessUser decodedUser = userJwtDecoderService.decodeTokenToUser(token);

        // then.
        assertThat(decodedUser.getRoles()).hasSize(1);
        assertThat(decodedUser.getRoles().stream()
                        .filter(it -> it.name().equals(role.name()))
                        .findFirst()
                        .orElseThrow()
                        .authorities())
                .extracting(Authority::getName)
                .containsExactlyInAnyOrderElementsOf(
                        role.authorities().stream().map(Authority::getName).collect(Collectors.toList()));
    }

    static Stream<Arguments> roles() {
        return Stream.of(Arguments.of(TestRoles.VIEWER), Arguments.of(TestRoles.EDITOR), Arguments.of(TestRoles.ADMIN));
    }

    @Test
    void shouldDecodeValidJwtToken_MultipleRoles() {
        User user = UserUtils.withPassword(
                USER_NAME, PASSWORD, Set.of(TestRoles.ADMIN, TestRoles.EDITOR, TestRoles.VIEWER));
        String token = userJwtEncoderService.generateToken(user);

        // when.
        PasswordlessUser decodedUser = userJwtDecoderService.decodeTokenToUser(token);

        // Admin
        assertThat(decodedUser.getRoles()).hasSize(3);
        assertThat(decodedUser.getRoles().stream()
                        .filter(role -> role.name().equals(TestRoles.ADMIN.name()))
                        .findFirst()
                        .orElseThrow()
                        .authorities())
                .extracting(Authority::getName)
                .containsExactlyInAnyOrderElementsOf(TestRoles.ADMIN.authorities().stream()
                        .map(Authority::getName)
                        .collect(Collectors.toList()));

        // Editor
        assertThat(decodedUser.getRoles().stream()
                        .filter(role -> role.name().equals(TestRoles.EDITOR.name()))
                        .findFirst()
                        .orElseThrow()
                        .authorities())
                .extracting(Authority::getName)
                .containsExactlyInAnyOrderElementsOf(TestRoles.EDITOR.authorities().stream()
                        .map(Authority::getName)
                        .collect(Collectors.toList()));

        // Viewer
        assertThat(decodedUser.getRoles().stream()
                        .filter(role -> role.name().equals(TestRoles.VIEWER.name()))
                        .findFirst()
                        .orElseThrow()
                        .authorities())
                .isEmpty();
    }

    @Test
    void shouldEncodeDecodeTokenWithHS256() {
        String key256 = "79912c6adb2a4f6c78a859807b072ce2a2c1140ac578f324cca983db22868b14";
        UserJwtEncoderService encoder = new DefaultUserJwtEncoderService(JwtAlgorithm.HMAC256, key256, lifespan);
        String token = encoder.generateToken(UserUtils.withPassword(USER_NAME, PASSWORD, Set.of(TestRoles.EDITOR)));
        UserJwtDecoderService decoder256 =
                new DefaultUserJwtDecoderService(authoritiesService, JwtAlgorithm.HMAC256, key256);

        // when.
        PasswordlessUser decodedUser = decoder256.decodeTokenToUser(token);

        // then.
        assertThat(decodedUser.getUsername()).isEqualTo(USER_NAME);
        assertThat(decodedUser.getRoles()).singleElement().satisfies(role -> {
            assertThat(role.name()).isEqualTo(TestRoles.EDITOR.name());
            assertThat(role.authorities())
                    .extracting(Authority::getName)
                    .containsExactlyInAnyOrderElementsOf(TestRoles.EDITOR.authorities().stream()
                            .map(Authority::getName)
                            .collect(Collectors.toList()));
        });
    }

    @Test
    void shouldEncodeDecodeTokenWithHS384() {
        String key384 =
                "bfa30eb1f16c07ba0a6a19a60f7c4bc02e1e10670411ae7a2f206b2bfe8801e2bb40741469d95fbbf4c86ae4b4a68437";
        UserJwtEncoderService encoder = new DefaultUserJwtEncoderService(JwtAlgorithm.HMAC384, key384, lifespan);
        String token = encoder.generateToken(UserUtils.withPassword(USER_NAME, PASSWORD, Set.of(TestRoles.ADMIN)));
        UserJwtDecoderService decoder384 =
                new DefaultUserJwtDecoderService(authoritiesService, JwtAlgorithm.HMAC384, key384);

        // when.
        PasswordlessUser decodedUser = decoder384.decodeTokenToUser(token);

        // then.
        assertThat(decodedUser.getUsername()).isEqualTo(USER_NAME);
        assertThat(decodedUser.getRoles()).singleElement().satisfies(role -> {
            assertThat(role.name()).isEqualTo(TestRoles.ADMIN.name());
            assertThat(role.authorities())
                    .extracting(Authority::getName)
                    .containsExactlyInAnyOrderElementsOf(TestRoles.ADMIN.authorities().stream()
                            .map(Authority::getName)
                            .collect(Collectors.toList()));
        });
    }

    @Test
    void shouldPreserveRegisteredCustomAuthority() {
        // given.
        jdbcClient
                .sql("INSERT INTO authorities (id, codename, description) VALUES (?, ?, ?)")
                .params("jwt-test-custom-authority", CustomAuthority.CUSTOM_AUTHORITY.getName(), "JWT test authority")
                .update();
        Role role = new DefaultRole("VIEWER", Set.of(CustomAuthority.CUSTOM_AUTHORITY));
        User user = UserUtils.withPassword(USER_NAME, PASSWORD, Set.of(role));
        String token = userJwtEncoderService.generateToken(user);

        // Use a fresh catalog because the shared service may have cached authorities before the insert.
        UserJwtDecoderService decoder = new DefaultUserJwtDecoderService(
                new DefaultAuthoritiesService(new AuthorityRepository(jdbcClient)), algorithm, signingKey);

        // when.
        PasswordlessUser decodedUser = decoder.decodeTokenToUser(token);

        // then.
        assertThat(decodedUser.getRoles())
                .first()
                .satisfies(r -> assertThat(r.authorities())
                        .extracting(Authority::getName)
                        .containsExactly(CustomAuthority.CUSTOM_AUTHORITY.getName()));
    }

    @ParameterizedTest
    @ValueSource(strings = {"TOTALLY_UNKNOWN_AUTHORITY", "INTERNAL.HEARTBEAT"})
    void shouldDropAuthorityUnknownToDecoder(String authorityName) {
        // given.
        Role role = new DefaultRole("VIEWER", Set.of((Authority) () -> authorityName));
        User user = UserUtils.withPassword(USER_NAME, PASSWORD, Set.of(role));
        String token = userJwtEncoderService.generateToken(user);

        // when.
        PasswordlessUser decodedUser = userJwtDecoderService.decodeTokenToUser(token);

        // then.
        assertThat(decodedUser.getRoles())
                .first()
                .satisfies(r -> assertThat(r.authorities()).isEmpty());
    }

    @Test
    void shouldDecodeValidJwtTokenWithoutUserRoles() {
        User user = UserUtils.withPassword(USER_NAME, PASSWORD, Set.of());
        String token = userJwtEncoderService.generateToken(user);
        PasswordlessUser decodedUser = userJwtDecoderService.decodeTokenToUser(token);

        assertThat(decodedUser.getUsername()).isEqualTo(USER_NAME);
        assertThat(decodedUser.getRoles()).isEmpty();
    }

    @Test
    void shouldThrowOnExpiredToken() {
        User user = UserUtils.withPassword(USER_NAME, PASSWORD, Set.of(TestRoles.ADMIN));
        String token = userJwtEncoderService.generateToken(user, Duration.ofSeconds(0));

        assertThatThrownBy(() -> userJwtDecoderService.decodeTokenToUser(token))
                .isInstanceOf(ExpiredJwtTokenException.class);
    }

    @Test
    void shouldThrowOnTamperedToken() {
        User user = UserUtils.withPassword(USER_NAME, PASSWORD, Set.of(TestRoles.ADMIN));
        String token = userJwtEncoderService.generateToken(user);

        assertThatThrownBy(() -> userJwtDecoderService.decodeTokenToUser(token + "x"))
                .isInstanceOf(InvalidJwtTokenException.class);
    }

    @Test
    void shouldFailToDecodeTokenWithWrongSecret() {
        String wrongSecret = "MX3TNBx0j8bGCjGWCvq1JffIqqzXLIV-URlKFLX4mfA";
        UserJwtEncoderService encoderWithWrongSecret =
                new DefaultUserJwtEncoderService(JwtAlgorithm.HMAC256, wrongSecret, lifespan);

        User user = UserUtils.withPassword(USER_NAME, PASSWORD, Set.of());
        String token = encoderWithWrongSecret.generateToken(user);

        assertThatThrownBy(() -> userJwtDecoderService.decodeTokenToUser(token))
                .isInstanceOf(InvalidJwtTokenException.class);
    }

    @Test
    void shouldPreserveUserIdWhenDecodingToken() {
        // given.
        String userId = "stable-id-42";
        User user = new DefaultUser(userId, USER_NAME, PASSWORD, Set.of(TestRoles.VIEWER));
        String token = userJwtEncoderService.generateToken(user);

        // when.
        PasswordlessUser decoded = userJwtDecoderService.decodeTokenToUser(token);

        // then.
        assertThat(decoded.getId()).isEqualTo(userId);
        assertThat(decoded.getUsername()).isEqualTo(USER_NAME);
    }

    @Test
    void shouldThrowInCaseUserIdIsNotPresentInJWT() {
        // given a token issued with no uid claim.
        String tokenWithNoUserId = Jwts.builder()
                .subject(USER_NAME)
                .claim(TokenClaim.ROLES.getEncoding(), List.of())
                .signWith(Keys.hmacShaKeyFor(signingKey.getBytes()))
                .compact();

        // when.
        ThrowableAssert.ThrowingCallable callable = () -> userJwtDecoderService.decodeTokenToUser(tokenWithNoUserId);

        // then.
        assertThatThrownBy(callable).isInstanceOf(JwtParsingException.class);
    }

    @Test
    void shouldDecodeAllRolesAndNestedComponents() {
        // given.
        Role parent = new DefaultRole("PARENT", Set.of(TestAuthorities.CACHES_CLEAR));
        Role child = new DefaultRole("CHILD", Set.of(TestAuthorities.CACHES_TOGGLE), Set.of(parent));
        PasswordlessUser user = new PasswordlessUser("bulk-user", "bulk", Set.of(parent, child));
        String token = userJwtEncoderService.generateToken(user);
        // when.
        PasswordlessUser decoded = userJwtDecoderService.decodeTokenToUser(token);
        // then.
        assertThat(decoded.getRoles())
                .flatExtracting(Role::getEffectiveAuthorities)
                .extracting(Authority::getName)
                .contains(TestAuthorities.CACHES_CLEAR.getName(), TestAuthorities.CACHES_TOGGLE.getName());
    }

    enum CustomAuthority implements Authority {
        CUSTOM_AUTHORITY;

        @Override
        public String getName() {
            return name();
        }
    }
}
