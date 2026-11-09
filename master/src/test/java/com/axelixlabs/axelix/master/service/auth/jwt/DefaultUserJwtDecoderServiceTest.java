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

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
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
import com.axelixlabs.axelix.master.domain.auth.DefaultAuthority;
import com.axelixlabs.axelix.master.domain.auth.DefaultRole;
import com.axelixlabs.axelix.master.domain.auth.DefaultUser;
import com.axelixlabs.axelix.master.domain.auth.PasswordlessUser;
import com.axelixlabs.axelix.master.domain.auth.Role;
import com.axelixlabs.axelix.master.domain.auth.TokenClaim;
import com.axelixlabs.axelix.master.domain.auth.User;
import com.axelixlabs.axelix.master.repository.AuthorityRepository;
import com.axelixlabs.axelix.master.service.auth.AuthoritiesService;
import com.axelixlabs.axelix.master.service.auth.DefaultAuthoritiesService;
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
    @BeforeEach
    void setUp() {
        Authority readReports = insertAuthority("jwt-test-report", "test:reports:read");
        Authority runMaintenance = insertAuthority("jwt-test-maintenance", "test:maintenance:run");
        reportingRole = new DefaultRole("REPORTING", Set.of(readReports, runMaintenance));
        maintenanceRole = new DefaultRole("MAINTENANCE", Set.of(runMaintenance));
        emptyRole = new DefaultRole("EMPTY", Set.of());
        authoritiesService = new DefaultAuthoritiesService(new AuthorityRepository(jdbcClient));
        userJwtDecoderService = new DefaultUserJwtDecoderService(authoritiesService, JwtAlgorithm.HMAC512, signingKey);
    }

    @AfterEach
    void deleteAuthorities() {
        jdbcClient
                .sql(
                        "DELETE FROM authorities WHERE id IN ('test-CACHES_CLEAR', 'test-CACHES_TOGGLE', 'jwt-test-custom-authority', 'jwt-test-report', 'jwt-test-maintenance')")
                .update();
    }

    private Role reportingRole;
    private Role maintenanceRole;
    private Role emptyRole;

    private static final String USER_NAME = "testUser";
    private static final String PASSWORD = "testPassword";

    @Value("${axelix.master.auth.jwt.lifespan}")
    private Duration lifespan;

    @Value("${axelix.master.auth.jwt.signing_key}")
    private String signingKey;

    private UserJwtDecoderService userJwtDecoderService;

    @Autowired
    private UserJwtEncoderService userJwtEncoderService;

    private AuthoritiesService authoritiesService;

    @Autowired
    private JdbcClient jdbcClient;

    @ParameterizedTest
    @ValueSource(strings = {"empty", "reporting", "maintenance"})
    void shouldDecodeValidJwtToken_ForAdHocRole(String scenario) {
        // given.
        Role role =
                switch (scenario) {
                    case "empty" -> emptyRole;
                    case "reporting" -> reportingRole;
                    default -> maintenanceRole;
                };
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

    @Test
    void shouldDecodeValidJwtToken_MultipleRoles() {
        User user = UserUtils.withPassword(USER_NAME, PASSWORD, Set.of(reportingRole, maintenanceRole, emptyRole));
        String token = userJwtEncoderService.generateToken(user);

        // when.
        PasswordlessUser decodedUser = userJwtDecoderService.decodeTokenToUser(token);

        // Reporting role.
        assertThat(decodedUser.getRoles()).hasSize(3);
        assertThat(decodedUser.getRoles().stream()
                        .filter(role -> role.name().equals(reportingRole.name()))
                        .findFirst()
                        .orElseThrow()
                        .authorities())
                .extracting(Authority::getName)
                .containsExactlyInAnyOrderElementsOf(reportingRole.authorities().stream()
                        .map(Authority::getName)
                        .collect(Collectors.toList()));

        // Maintenance role.
        assertThat(decodedUser.getRoles().stream()
                        .filter(role -> role.name().equals(maintenanceRole.name()))
                        .findFirst()
                        .orElseThrow()
                        .authorities())
                .extracting(Authority::getName)
                .containsExactlyInAnyOrderElementsOf(maintenanceRole.authorities().stream()
                        .map(Authority::getName)
                        .collect(Collectors.toList()));

        // Empty role.
        assertThat(decodedUser.getRoles().stream()
                        .filter(role -> role.name().equals(emptyRole.name()))
                        .findFirst()
                        .orElseThrow()
                        .authorities())
                .isEmpty();
    }

    @Test
    void shouldEncodeDecodeTokenWithHS256() {
        String key256 = "79912c6adb2a4f6c78a859807b072ce2a2c1140ac578f324cca983db22868b14";
        UserJwtEncoderService encoder = new DefaultUserJwtEncoderService(JwtAlgorithm.HMAC256, key256, lifespan);
        String token = encoder.generateToken(UserUtils.withPassword(USER_NAME, PASSWORD, Set.of(maintenanceRole)));
        UserJwtDecoderService decoder256 =
                new DefaultUserJwtDecoderService(authoritiesService, JwtAlgorithm.HMAC256, key256);

        // when.
        PasswordlessUser decodedUser = decoder256.decodeTokenToUser(token);

        // then.
        assertThat(decodedUser.getUsername()).isEqualTo(USER_NAME);
        assertThat(decodedUser.getRoles()).singleElement().satisfies(role -> {
            assertThat(role.name()).isEqualTo(maintenanceRole.name());
            assertThat(role.authorities())
                    .extracting(Authority::getName)
                    .containsExactlyInAnyOrderElementsOf(maintenanceRole.authorities().stream()
                            .map(Authority::getName)
                            .collect(Collectors.toList()));
        });
    }

    @Test
    void shouldEncodeDecodeTokenWithHS384() {
        String key384 =
                "bfa30eb1f16c07ba0a6a19a60f7c4bc02e1e10670411ae7a2f206b2bfe8801e2bb40741469d95fbbf4c86ae4b4a68437";
        UserJwtEncoderService encoder = new DefaultUserJwtEncoderService(JwtAlgorithm.HMAC384, key384, lifespan);
        String token = encoder.generateToken(UserUtils.withPassword(USER_NAME, PASSWORD, Set.of(reportingRole)));
        UserJwtDecoderService decoder384 =
                new DefaultUserJwtDecoderService(authoritiesService, JwtAlgorithm.HMAC384, key384);

        // when.
        PasswordlessUser decodedUser = decoder384.decodeTokenToUser(token);

        // then.
        assertThat(decodedUser.getUsername()).isEqualTo(USER_NAME);
        assertThat(decodedUser.getRoles()).singleElement().satisfies(role -> {
            assertThat(role.name()).isEqualTo(reportingRole.name());
            assertThat(role.authorities())
                    .extracting(Authority::getName)
                    .containsExactlyInAnyOrderElementsOf(reportingRole.authorities().stream()
                            .map(Authority::getName)
                            .collect(Collectors.toList()));
        });
    }

    @Test
    void shouldPreserveRegisteredCustomAuthority() {
        // given.
        Authority customAuthority = insertAuthority("jwt-test-custom-authority", "jwt:test:custom");
        Role role = new DefaultRole("CUSTOM", Set.of(customAuthority));
        User user = UserUtils.withPassword(USER_NAME, PASSWORD, Set.of(role));
        String token = userJwtEncoderService.generateToken(user);

        // when.
        PasswordlessUser decodedUser = userJwtDecoderService.decodeTokenToUser(token);

        // then.
        assertThat(decodedUser.getRoles())
                .first()
                .satisfies(r -> assertThat(r.authorities())
                        .extracting(Authority::getName)
                        .containsExactly("jwt:test:custom"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"TOTALLY_UNKNOWN_AUTHORITY", "INTERNAL.HEARTBEAT"})
    void shouldDropAuthorityUnknownToDecoder(String authorityName) {
        // given.
        Role role = new DefaultRole("CUSTOM", Set.of(() -> authorityName));
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
        User user = UserUtils.withPassword(USER_NAME, PASSWORD, Set.of(reportingRole));
        String token = userJwtEncoderService.generateToken(user, Duration.ofSeconds(0));

        assertThatThrownBy(() -> userJwtDecoderService.decodeTokenToUser(token))
                .isInstanceOf(ExpiredJwtTokenException.class);
    }

    @Test
    void shouldThrowOnTamperedToken() {
        User user = UserUtils.withPassword(USER_NAME, PASSWORD, Set.of(reportingRole));
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
        User user = new DefaultUser(userId, USER_NAME, PASSWORD, Set.of(emptyRole));
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
        Authority cachesClear = insertAuthority("test-CACHES_CLEAR", "test:caches:clear");
        Authority cachesToggle = insertAuthority("test-CACHES_TOGGLE", "test:caches:toggle");
        Role parent = new DefaultRole("PARENT", Set.of(cachesClear));
        Role child = new DefaultRole("CHILD", Set.of(cachesToggle), Set.of(parent));
        PasswordlessUser user = new PasswordlessUser("bulk-user", "bulk", Set.of(parent, child));
        String token = userJwtEncoderService.generateToken(user);
        // when.
        PasswordlessUser decoded = userJwtDecoderService.decodeTokenToUser(token);
        // then.
        assertThat(decoded.getRoles())
                .flatExtracting(Role::getEffectiveAuthorities)
                .extracting(Authority::getName)
                .contains("test:caches:clear", "test:caches:toggle");
    }

    private Authority insertAuthority(String id, String codename) {
        jdbcClient
                .sql("INSERT INTO authorities (id, codename, description) VALUES (?, ?, ?)")
                .params(id, codename, "Test authority")
                .update();
        return jdbcClient
                .sql("SELECT id, codename FROM authorities WHERE id = ?")
                .param(id)
                .query((rs, n) -> new DefaultAuthority(rs.getString("id"), rs.getString("codename")))
                .single();
    }
}
