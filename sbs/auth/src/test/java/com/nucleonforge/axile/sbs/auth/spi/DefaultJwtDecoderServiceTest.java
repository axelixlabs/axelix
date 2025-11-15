package com.nucleonforge.axile.sbs.auth.spi;

import java.util.Set;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import com.nucleonforge.axile.common.auth.core.DefaultAuthority;
import com.nucleonforge.axile.common.auth.core.DefaultRole;
import com.nucleonforge.axile.common.auth.core.Role;
import com.nucleonforge.axile.common.auth.spi.jwt.JwtAlgorithm;
import com.nucleonforge.axile.sbs.auth.model.DecodedUser;
import com.nucleonforge.axile.sbs.auth.spi.jwt.exception.ExpiredJwtTokenException;
import com.nucleonforge.axile.sbs.auth.spi.jwt.exception.InvalidJwtTokenException;
import com.nucleonforge.axile.sbs.auth.spi.jwt.service.DefaultJwtDecoderService;
import com.nucleonforge.axile.sbs.auth.spi.jwt.service.JwtDecoderService;

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

        Role userRole = decodedUser.roles().stream()
                .filter(role -> role.name().equals("ROLE_USER"))
                .findFirst()
                .orElseThrow();

        Role engineerRole = decodedUser.roles().stream()
                .filter(role -> role.name().equals("ROLE_ENGINEER"))
                .findFirst()
                .orElseThrow();

        assertThat(userRole.authorities()).containsAll(Set.of(DefaultAuthority.ENV, DefaultAuthority.INFO));
        assertThat(engineerRole.authorities()).containsAll(Set.of(DefaultAuthority.BEANS, DefaultAuthority.HEALTH));
    }

    @Test
    void shouldDecodeValidJwtToken_WithRoleHierarchy() {
        DecodedUser decodedUser = jwtDecoderService.decodeTokenToUser(tokenUserWithAdminRoleHierarchy);

        Role rootRole = decodedUser.roles().stream()
                .filter(role -> role.name().equals("ROLE_ROOT"))
                .findFirst()
                .orElseThrow();

        assertThat(rootRole.components()).hasSize(2);

        Role adminRole = rootRole.components().stream()
                .filter(role -> role.name().equals("ROLE_ADMIN"))
                .findFirst()
                .orElseThrow();

        assertThat(adminRole.authorities()).contains(DefaultAuthority.PROFILE_MANAGEMENT);

        Role engineerRole = adminRole.components().iterator().next();

        assertThat(engineerRole.name()).isEqualTo("ROLE_ENGINEER");
        assertThat(engineerRole.authorities()).isEqualTo(Set.of(DefaultAuthority.ENV));

        Role userRole = engineerRole.components().iterator().next();

        assertThat(userRole.name()).isEqualTo("ROLE_USER");
        assertThat(userRole.authorities()).isEqualTo(Set.of(DefaultAuthority.INFO));

        Role readRole = rootRole.components().stream()
                .filter(role -> role.name().equals("ROLE_READ"))
                .findFirst()
                .orElseThrow();

        assertThat(readRole.authorities()).contains(DefaultAuthority.BEANS);
    }

    @Test
    void shouldEncodeDecodeTokenWithHS256() {
        String key256 = "79912c6adb2a4f6c78a859807b072ce2a2c1140ac578f324cca983db22868b14";
        JwtDecoderService decoder256 = new DefaultJwtDecoderService(JwtAlgorithm.HMAC256, key256);

        DecodedUser expectedUser = new DecodedUser(
                "testUser", Set.of(new DefaultRole("ROLE_USER", Set.of(DefaultAuthority.MAPPINGS), Set.of())));

        DecodedUser decodedUser = decoder256.decodeTokenToUser(tokenWithHs256Algorithm);

        assertThat(decodedUser).usingRecursiveComparison().isEqualTo(expectedUser);
    }

    @Test
    void shouldEncodeDecodeTokenWithHS384() {
        String key384 =
                "bfa30eb1f16c07ba0a6a19a60f7c4bc02e1e10670411ae7a2f206b2bfe8801e2bb40741469d95fbbf4c86ae4b4a68437";
        JwtDecoderService decoder384 = new DefaultJwtDecoderService(JwtAlgorithm.HMAC384, key384);

        DecodedUser expectedUser = new DecodedUser(
                "testUser", Set.of(new DefaultRole("ROLE_USER", Set.of(DefaultAuthority.BEANS), Set.of())));

        DecodedUser decodedUser = decoder384.decodeTokenToUser(tokenWithHs384Algorithm);

        assertThat(decodedUser).usingRecursiveComparison().isEqualTo(expectedUser);
    }

    @Test
    void shouldOmitInvalidAuthority() {
        DecodedUser decodedUser = jwtDecoderService.decodeTokenToUser(tokenUserWithUnrecognizedAuthorities);

        assertThat(decodedUser.roles())
                .first()
                .satisfies(role -> assertThat(role.authorities()).hasSize(1).containsOnly(DefaultAuthority.ENV));
    }

    @Test
    void shouldDecodeValidJwtTokenWithoutUserRoles() {
        DecodedUser decodedUser = jwtDecoderService.decodeTokenToUser(tokenWithEmptyRoles);

        assertThat(decodedUser.username()).isEqualTo("userWithEmptyRoles");
        assertThat(decodedUser.roles()).isEmpty();
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
