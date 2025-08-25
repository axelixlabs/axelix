package com.nucleonforge.axile.master.auth.spi.jwt;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.util.ReflectionTestUtils;

import com.nucleonforge.axile.common.auth.core.DefaultAuthority;
import com.nucleonforge.axile.common.auth.core.DefaultRole;
import com.nucleonforge.axile.common.auth.core.DefaultUser;
import com.nucleonforge.axile.common.auth.core.Role;
import com.nucleonforge.axile.common.auth.core.User;
import com.nucleonforge.axile.common.auth.spi.jwt.JwtAlgorithm;
import com.nucleonforge.axile.master.auth.spi.jwt.service.DefaultJwtEncoderService;
import com.nucleonforge.axile.master.auth.spi.jwt.service.JwtEncoderService;
import com.nucleonforge.axile.master.auth.spi.jwt.service.JwtTokenGenerationException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Integration tests for {@link JwtEncoderService}, verifying correct JWT generation logic.
 *
 * @author Nikita Kirillov
 * @since 22.07.2025
 */
@SpringBootTest
@Import(DefaultJwtEncoderServiceTest.JwtEncoderServiceConfig.class)
@EnableAutoConfiguration(exclude = DataSourceAutoConfiguration.class)
class DefaultJwtEncoderServiceTest {

    @Autowired
    private JwtEncoderService jwtEncoderService;

    @Value("${axile.master.auth.jwt.signing_key}")
    private String signingKey;

    @Value("${axile.master.auth.jwt.lifespan}")
    private Duration lifespan;

    @Test
    void shouldGenerateTokenWithRequiredClaims() {
        User user = new DefaultUser("testUser", Collections.emptySet());
        String token = jwtEncoderService.generateToken(user);

        Jws<Claims> claims = Jwts.parser()
                .verifyWith(Keys.hmacShaKeyFor(signingKey.getBytes()))
                .build()
                .parseSignedClaims(token);

        assertEquals("testUser", claims.getPayload().getSubject());
        assertNotNull(claims.getPayload().getExpiration());
        assertNotNull(claims.getPayload().getIssuedAt());
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldGenerateValidJwtToken() {
        Role role = new DefaultRole(
                "testRole", Set.of(DefaultAuthority.ENV, DefaultAuthority.BEANS), Collections.emptySet());
        User user = new DefaultUser("testUser", Set.of(role));

        String token = jwtEncoderService.generateToken(user);

        Claims claims = Jwts.parser()
                .verifyWith(Keys.hmacShaKeyFor(signingKey.getBytes()))
                .build()
                .parseSignedClaims(token)
                .getPayload();

        assertEquals("testUser", claims.getSubject());

        List<Map<String, Object>> roles = claims.get("roles", List.class);
        assertNotNull(roles);
        assertEquals(1, roles.size());

        Set<String> authorities = new HashSet<>((List<String>) roles.get(0).get("authorities"));
        assertTrue(authorities.contains("ENV"));
        assertTrue(authorities.contains("BEANS"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldGenerateValidJwtTokenMultipleRoles() {
        Role role1 = new DefaultRole(
                "firstTestRole", Set.of(DefaultAuthority.HEALTH, DefaultAuthority.INFO), Collections.emptySet());
        Role role2 = new DefaultRole("secondTestRole", Set.of(DefaultAuthority.BEANS), Collections.emptySet());

        User user = new DefaultUser("multiRoleUser", Set.of(role1, role2));

        String token = jwtEncoderService.generateToken(user);

        Claims claims = Jwts.parser()
                .verifyWith(Keys.hmacShaKeyFor(signingKey.getBytes()))
                .build()
                .parseSignedClaims(token)
                .getPayload();

        List<Map<String, Object>> roles = claims.get("roles", List.class);
        assertEquals(2, roles.size());

        Set<String> allAuthorities = roles.stream()
                .flatMap(r -> ((List<String>) r.get("authorities")).stream())
                .collect(Collectors.toSet());

        assertEquals(3, allAuthorities.size());
        assertTrue(allAuthorities.contains("BEANS"));
        assertTrue(allAuthorities.contains("HEALTH"));
        assertTrue(allAuthorities.contains("INFO"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldGenerateValidJwtToken_WithRoleHierarchy() {
        Role adminRole = createAdminRoleHierarchy();
        Role rootRole = new DefaultRole("rootRole", Set.of(DefaultAuthority.HEALTH), Set.of(adminRole));
        User user = new DefaultUser("multiRoleUser", Set.of(rootRole));

        String token = jwtEncoderService.generateToken(user);

        Claims claims = Jwts.parser()
                .verifyWith(Keys.hmacShaKeyFor(signingKey.getBytes()))
                .build()
                .parseSignedClaims(token)
                .getPayload();

        List<Map<String, Object>> roles = claims.get("roles", List.class);
        assertEquals(1, roles.size());

        Map<String, Object> rootRoleMap = roles.get(0);
        assertEquals("rootRole", rootRoleMap.get("name"));
        assertTrue(((List<String>) rootRoleMap.get("authorities")).contains("HEALTH"));

        // root -> admin
        List<Map<String, Object>> adminComponents = (List<Map<String, Object>>) rootRoleMap.get("components");
        assertEquals(1, adminComponents.size());

        Map<String, Object> adminRoleMap = adminComponents.get(0);
        assertEquals("admin", adminRoleMap.get("name"));
        assertTrue(((List<String>) adminRoleMap.get("authorities")).contains("PROFILE_MANAGEMENT"));

        // admin -> engineer, cacheDispatcher
        List<Map<String, Object>> adminSubComponents = (List<Map<String, Object>>) adminRoleMap.get("components");
        assertEquals(2, adminSubComponents.size());

        // admin -> engineer -> user
        Map<String, Object> engineerRoleMap = adminSubComponents.stream()
                .filter(m -> m.get("name").equals("engineer"))
                .findFirst()
                .orElseThrow();
        assertTrue(((List<String>) engineerRoleMap.get("authorities")).contains("ENV"));

        List<Map<String, Object>> engineerComponents = (List<Map<String, Object>>) engineerRoleMap.get("components");
        assertEquals(1, engineerComponents.size());

        Map<String, Object> userRoleMap = engineerComponents.get(0);
        assertEquals("user", userRoleMap.get("name"));
        assertTrue(((List<String>) userRoleMap.get("authorities")).contains("INFO"));

        // admin -> cacheDispatcher -> cacheManager
        Map<String, Object> cacheDispatcherMap = adminSubComponents.stream()
                .filter(m -> m.get("name").equals("cacheDispatcherRole"))
                .findFirst()
                .orElseThrow();
        assertTrue(((List<String>) cacheDispatcherMap.get("authorities")).contains("CACHE_DISPATCHER"));

        List<Map<String, Object>> dispatcherComponents =
                (List<Map<String, Object>>) cacheDispatcherMap.get("components");
        assertEquals(1, dispatcherComponents.size());

        Map<String, Object> cacheManagerMap = dispatcherComponents.get(0);
        assertEquals("cacheAccessRole", cacheManagerMap.get("name"));
        assertTrue(((List<String>) cacheManagerMap.get("authorities")).contains("CACHES"));
    }

    @Test
    void shouldContainCorrectExpirationTime() {
        User user = new DefaultUser("expUser", Set.of());

        String token = jwtEncoderService.generateToken(user);

        Claims claims = Jwts.parser()
                .verifyWith(Keys.hmacShaKeyFor(signingKey.getBytes()))
                .build()
                .parseSignedClaims(token)
                .getPayload();

        long actualExpiration =
                claims.getExpiration().getTime() - claims.getIssuedAt().getTime();
        assertTrue(actualExpiration > 0 && actualExpiration <= lifespan.toMillis());
    }

    @Test
    void shouldHandleNullUser() {
        JwtTokenGenerationException exception =
                assertThrows(JwtTokenGenerationException.class, () -> jwtEncoderService.generateToken(null));

        assertEquals("User cannot be null", exception.getMessage());
    }

    @Test
    void shouldThrowWhenUsernameIsNull() {
        User user = new DefaultUser(null, Set.of());

        JwtTokenGenerationException exception =
                assertThrows(JwtTokenGenerationException.class, () -> jwtEncoderService.generateToken(user));

        assertEquals("Username cannot be null or empty", exception.getMessage());
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldGenerateTokenForUserWithoutRoles() {
        User user = new DefaultUser("userWithoutRoles", null);

        String token = jwtEncoderService.generateToken(user);

        Claims claims = Jwts.parser()
                .verifyWith(Keys.hmacShaKeyFor(signingKey.getBytes()))
                .build()
                .parseSignedClaims(token)
                .getPayload();

        assertEquals("userWithoutRoles", claims.getSubject());

        List<Map<String, Object>> roles = claims.get("roles", List.class);
        assertNotNull(roles);
        assertTrue(roles.isEmpty());
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldGenerateTokenForUserWithRoleThatHasNoAuthorities() {
        Role roleWithoutAuthorities =
                new DefaultRole("roleWithoutAuthorities", Collections.emptySet(), Collections.emptySet());
        User user = new DefaultUser("userWithEmptyAuthorities", Set.of(roleWithoutAuthorities));

        String token = jwtEncoderService.generateToken(user);

        Claims claims = Jwts.parser()
                .verifyWith(Keys.hmacShaKeyFor(signingKey.getBytes()))
                .build()
                .parseSignedClaims(token)
                .getPayload();

        assertEquals("userWithEmptyAuthorities", claims.getSubject());

        List<Map<String, Object>> roles = claims.get("roles", List.class);
        assertNotNull(roles);
        assertEquals(1, roles.size());

        List<String> authorities = (List<String>) roles.get(0).get("authorities");
        assertNotNull(authorities);
        assertTrue(authorities.isEmpty());
    }

    @Test
    void shouldFailWithInvalidSecretKey() {
        String shortSecretKey = "shortKey";
        JwtAlgorithm jwtAlgorithm = JwtAlgorithm.HMAC256;
        DefaultJwtEncoderService invalidService =
                new DefaultJwtEncoderService(jwtAlgorithm, shortSecretKey, Duration.ofDays(3));
        ReflectionTestUtils.setField(invalidService, "signingKey", shortSecretKey);
        ReflectionTestUtils.setField(invalidService, "lifespan", lifespan);

        User user = new DefaultUser("invalidKeyUser", Set.of());

        JwtTokenGenerationException exception =
                assertThrows(JwtTokenGenerationException.class, () -> invalidService.generateToken(user));

        System.out.println(exception.getMessage());
        assertTrue(exception
                .getMessage()
                .contains("The secret key is too weak for " + jwtAlgorithm.getAlgorithmName()
                        + " algorithm. It must be at least " + jwtAlgorithm.getMinKeyLength() + " bytes."));
    }

    @Test
    void shouldGenerateTokenWithHs256() {
        String hs256Key = "6f0ac45fa8c1358a9c6acf6af78ec7bbd984af99c7fd1e9220304624d29105b3";
        JwtAlgorithm algorithm = JwtAlgorithm.HMAC256;
        JwtEncoderService encoder = new DefaultJwtEncoderService(algorithm, hs256Key, lifespan);

        User user = new DefaultUser("hs256User", Set.of());

        String token = encoder.generateToken(user);

        Claims claims = Jwts.parser()
                .verifyWith(Keys.hmacShaKeyFor(hs256Key.getBytes(StandardCharsets.UTF_8)))
                .build()
                .parseSignedClaims(token)
                .getPayload();

        assertEquals("hs256User", claims.getSubject());
        assertNotNull(claims.getIssuedAt());
        assertNotNull(claims.getExpiration());
    }

    @Test
    void shouldGenerateTokenWithHs384() {
        String hs384Key =
                "4eff557c362950836cd5685c80e82197e914811d7589da1248477a22423665c546ab3700b424587576d4b20180d7234b";
        JwtAlgorithm algorithm = JwtAlgorithm.HMAC384;
        JwtEncoderService encoder = new DefaultJwtEncoderService(algorithm, hs384Key, lifespan);

        User user = new DefaultUser("hs384User", Set.of());

        String token = encoder.generateToken(user);

        Claims claims = Jwts.parser()
                .verifyWith(Keys.hmacShaKeyFor(hs384Key.getBytes(StandardCharsets.UTF_8)))
                .build()
                .parseSignedClaims(token)
                .getPayload();

        assertEquals("hs384User", claims.getSubject());
        assertNotNull(claims.getIssuedAt());
        assertNotNull(claims.getExpiration());
    }

    @Test
    void shouldGenerateProperlyFormattedToken() {
        User user = new DefaultUser("formatTest", Set.of());
        String token = jwtEncoderService.generateToken(user);

        String[] parts = token.split("\\.");
        assertEquals(3, parts.length);
    }

    private Role createAdminRoleHierarchy() {
        Role cacheAccessRole =
                new DefaultRole("cacheAccessRole", Set.of(DefaultAuthority.CACHES), Collections.emptySet());
        Role cacheDispatcherRole = new DefaultRole(
                "cacheDispatcherRole", Set.of(DefaultAuthority.CACHE_DISPATCHER), Set.of(cacheAccessRole));
        Role userRole = new DefaultRole("user", Set.of(DefaultAuthority.INFO), Collections.emptySet());
        Role engineerRole = new DefaultRole("engineer", Set.of(DefaultAuthority.ENV), Set.of(userRole));
        return new DefaultRole(
                "admin", Set.of(DefaultAuthority.PROFILE_MANAGEMENT), Set.of(engineerRole, cacheDispatcherRole));
    }

    /**
     * Minimal test configuration for {@link JwtEncoderService} integration testing.
     *
     * <p>Registers a {@link JwtEncoderService} bean for use in test cases. Relies on external configuration
     * properties {@code axile.master.auth.jwt.algorithm}, {@code axile.master.auth.jwt.signing_key}
     * and {@code axile.master.auth.jwt.lifespan} to initialize the service.</p>
     */
    @TestConfiguration
    public static class JwtEncoderServiceConfig {

        @Bean
        public JwtEncoderService jwtEncoderService(
                final @Value("${axile.master.auth.jwt.algorithm}") JwtAlgorithm algorithm,
                final @Value("${axile.master.auth.jwt.signing_key}") String signingKey,
                final @Value("${axile.master.auth.jwt.lifespan}") Duration lifespan) {
            return new DefaultJwtEncoderService(algorithm, signingKey, lifespan);
        }
    }
}
