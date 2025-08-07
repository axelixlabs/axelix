package com.nucleonforge.axile.auth.spi;

import java.util.Set;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

import com.nucleonforge.axile.auth.spi.jwt.exception.ExpiredJwtTokenException;
import com.nucleonforge.axile.auth.spi.jwt.exception.InvalidJwtTokenException;
import com.nucleonforge.axile.auth.spi.jwt.service.DefaultJwtDecoderService;
import com.nucleonforge.axile.auth.spi.jwt.service.JwtDecoderService;
import com.nucleonforge.axile.common.auth.core.DefaultAuthority;
import com.nucleonforge.axile.common.auth.core.DefaultRole;
import com.nucleonforge.axile.common.auth.core.DefaultUser;
import com.nucleonforge.axile.common.auth.core.Role;
import com.nucleonforge.axile.common.auth.core.User;
import com.nucleonforge.axile.common.auth.spi.jwt.JwtAlgorithm;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Integration tests for {@link JwtDecoderService}, verifying correct decoding and validation of JWT tokens.
 *
 * @author Nikita Kirillov
 * @since 22.07.2025
 */
@SpringBootTest
@Import(DefaultJwtDecoderServiceTest.JwtDecoderServiceConfig.class)
@EnableAutoConfiguration(exclude = DataSourceAutoConfiguration.class)
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

    @Test
    void shouldDecodeValidJwtToken() {
        User decodedUser = jwtDecoderService.decodeTokenToUser(tokenUserWithTwoRole);

        Role userRole = decodedUser.roles().stream()
                .filter(role -> role.name().equals("ROLE_USER"))
                .findFirst()
                .orElseThrow();

        Role engineerRole = decodedUser.roles().stream()
                .filter(role -> role.name().equals("ROLE_ENGINEER"))
                .findFirst()
                .orElseThrow();

        assertTrue(userRole.authorities().containsAll(Set.of(DefaultAuthority.ENV, DefaultAuthority.INFO)));
        assertTrue(engineerRole.authorities().containsAll(Set.of(DefaultAuthority.BEANS, DefaultAuthority.HEALTH)));
        assertEquals("testUser", decodedUser.username());
    }

    @Test
    void shouldDecodeValidJwtToken_WithRoleHierarchy() {
        User decodedUser = jwtDecoderService.decodeTokenToUser(tokenUserWithAdminRoleHierarchy);

        Role decodedRootRole = decodedUser.roles().stream()
                .filter(r -> r.name().equals("ROLE_ROOT"))
                .findFirst()
                .orElseThrow();

        assertEquals(2, decodedRootRole.components().size());

        Role decodedAdmin = decodedRootRole.components().stream()
                .filter(r -> r.name().equals("ROLE_ADMIN"))
                .findFirst()
                .orElseThrow();

        assertTrue(decodedAdmin.authorities().contains(DefaultAuthority.PROFILE_MANAGEMENT));

        Role decodedEngineer = decodedAdmin.components().iterator().next();
        assertEquals("ROLE_ENGINEER", decodedEngineer.name());
        assertTrue(decodedEngineer.authorities().contains(DefaultAuthority.ENV));

        Role decodedUserRole = decodedEngineer.components().iterator().next();
        assertEquals("ROLE_USER", decodedUserRole.name());
        assertTrue(decodedUserRole.authorities().contains(DefaultAuthority.INFO));

        Role decodedReadRole = decodedRootRole.components().stream()
                .filter(r -> r.name().equals("ROLE_READ"))
                .findFirst()
                .orElseThrow();

        assertTrue(decodedReadRole.authorities().contains(DefaultAuthority.BEANS));
    }

    @Test
    void shouldEncodeDecodeTokenWithHS256() {
        String key256 = "79912c6adb2a4f6c78a859807b072ce2a2c1140ac578f324cca983db22868b14";
        JwtDecoderService decoder256 = new DefaultJwtDecoderService(JwtAlgorithm.HMAC256, key256);

        User user = new DefaultUser(
                "testUser", Set.of(new DefaultRole("ROLE_USER", Set.of(DefaultAuthority.MAPPINGS), Set.of())));

        User decodedUser = decoder256.decodeTokenToUser(tokenWithHs256Algorithm);

        assertEquals(user, decodedUser);
    }

    @Test
    void shouldEncodeDecodeTokenWithHS384() {
        String key384 =
                "bfa30eb1f16c07ba0a6a19a60f7c4bc02e1e10670411ae7a2f206b2bfe8801e2bb40741469d95fbbf4c86ae4b4a68437";
        JwtDecoderService decoder384 = new DefaultJwtDecoderService(JwtAlgorithm.HMAC384, key384);

        User user = new DefaultUser(
                "testUser", Set.of(new DefaultRole("ROLE_USER", Set.of(DefaultAuthority.BEANS), Set.of())));

        User decodedUser = decoder384.decodeTokenToUser(tokenWithHs384Algorithm);

        assertEquals(user, decodedUser);
    }

    @Test
    void shouldDecodeValidJwtTokenWithoutUserRoles() {
        User decodedUser = jwtDecoderService.decodeTokenToUser(tokenWithEmptyRoles);

        assertEquals("userWithEmptyRoles", decodedUser.username());
        assertTrue(decodedUser.roles().isEmpty());
    }

    @Test
    void shouldThrowOnExpiredToken() {
        ExpiredJwtTokenException exception =
                assertThrows(ExpiredJwtTokenException.class, () -> jwtDecoderService.decodeTokenToUser(expiredToken));

        assertTrue(exception.getMessage().startsWith("JWT token has expired"));
    }

    @Test
    void shouldThrowOnTamperedToken() {
        InvalidJwtTokenException exception = assertThrows(
                InvalidJwtTokenException.class,
                () -> jwtDecoderService.decodeTokenToUser(tokenUserWithAdminRoleHierarchy + "x"));

        assertEquals("JWT token is invalid or tampered", exception.getMessage());
    }

    @Test
    void shouldFailToDecodeTokenWithWrongSecret() {
        InvalidJwtTokenException exception = assertThrows(
                InvalidJwtTokenException.class, () -> jwtDecoderService.decodeTokenToUser(tokenSignedWithWrongKey));

        assertEquals("JWT token is invalid or tampered", exception.getMessage());
    }

    /**
     * Minimal test configuration for {@link JwtDecoderService} integration testing.
     *
     * <p>Registers beans for {@link JwtDecoderService}, allowing
     * full-stack testing of JWT encoding and decoding within a Spring context.</p>
     */
    @TestConfiguration
    public static class JwtDecoderServiceConfig {

        @Bean
        public JwtDecoderService jwtDecoderService(
                final @Value("${axile.master.auth.jwt.algorithm}") JwtAlgorithm algorithm,
                final @Value("${axile.master.auth.jwt.signing-key}") String signingKey) {
            return new DefaultJwtDecoderService(algorithm, signingKey);
        }
    }
}
