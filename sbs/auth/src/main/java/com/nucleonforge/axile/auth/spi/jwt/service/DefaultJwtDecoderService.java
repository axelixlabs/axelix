package com.nucleonforge.axile.auth.spi.jwt.service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nucleonforge.axile.auth.spi.jwt.exception.ExpiredJwtTokenException;
import com.nucleonforge.axile.auth.spi.jwt.exception.InvalidJwtTokenException;
import com.nucleonforge.axile.auth.spi.jwt.exception.JwtParsingException;
import com.nucleonforge.axile.auth.spi.jwt.exception.JwtTokenDecodingException;
import com.nucleonforge.axile.auth.spi.jwt.verification.JwtVerificationStrategy;
import com.nucleonforge.axile.auth.spi.jwt.verification.JwtVerificationStrategyFactory;
import com.nucleonforge.axile.common.auth.core.Authority;
import com.nucleonforge.axile.common.auth.core.DefaultAuthority;
import com.nucleonforge.axile.common.auth.core.DefaultRole;
import com.nucleonforge.axile.common.auth.core.DefaultUser;
import com.nucleonforge.axile.common.auth.core.Role;
import com.nucleonforge.axile.common.auth.core.User;
import com.nucleonforge.axile.common.auth.spi.jwt.JwtAlgorithm;
import com.nucleonforge.axile.common.auth.spi.jwt.TokenClaim;

/**
 * Default implementation of {@link JwtDecoderService}.
 *
 * @since 22.07.2025
 * @author Nikita Kirillov
 */
public class DefaultJwtDecoderService implements JwtDecoderService {

    private static final Logger logger = LoggerFactory.getLogger(DefaultJwtDecoderService.class);

    private final JwtVerificationStrategy verificationStrategy;

    private final String signingKey;

    public DefaultJwtDecoderService(JwtAlgorithm algorithm, String signingKey) {
        this.verificationStrategy = JwtVerificationStrategyFactory.createVerificationStrategy(algorithm);
        this.signingKey = Objects.requireNonNull(signingKey);
    }

    @Override
    public User decodeTokenToUser(String token)
            throws ExpiredJwtTokenException, InvalidJwtTokenException, JwtTokenDecodingException, JwtParsingException {

        try {
            Claims claims = parseClaims(token).getPayload();
            return new DefaultUser(claims.getSubject(), extractRoles(claims));
        } catch (JwtParsingException e) {
            throw e;
        } catch (ExpiredJwtException e) {
            throw new ExpiredJwtTokenException("JWT token has expired", e);
        } catch (JwtException e) {
            throw new InvalidJwtTokenException("JWT token is invalid or tampered", e);
        } catch (Exception e) {
            throw new JwtTokenDecodingException("Unexpected error while decoding JWT token", e);
        }
    }

    private Jws<Claims> parseClaims(String token) {
        return verificationStrategy.verifyAndParse(token, signingKey);
    }

    private Set<Role> extractRoles(Claims claims) {
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> rolesClaim = claims.get(TokenClaim.ROLES.getEncoding(), List.class);

        return Optional.ofNullable(rolesClaim).orElse(List.of()).stream()
                .map(this::mapToRole)
                .collect(Collectors.toSet());
    }

    private Role mapToRole(Map<String, Object> roleMap) {
        String roleName = (String) roleMap.get(TokenClaim.ROLE_NAME.getEncoding());

        if (roleName == null) {
            throw new JwtParsingException("Role name is null in JWT token");
        }

        @SuppressWarnings("unchecked")
        List<String> authoritiesList =
                (List<String>) roleMap.getOrDefault(TokenClaim.AUTHORITIES.getEncoding(), List.of());

        Set<Authority> authorities = authoritiesList.stream()
                .map(this::safeAuthoritiesFromString)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> components =
                (List<Map<String, Object>>) roleMap.getOrDefault("components", List.of());

        Set<Role> componentRoles = components.stream().map(this::mapToRole).collect(Collectors.toSet());

        return new DefaultRole(roleName, authorities, componentRoles);
    }

    @Nullable
    private DefaultAuthority safeAuthoritiesFromString(String name) {
        try {
            return DefaultAuthority.valueOf(name);
        } catch (IllegalArgumentException ignored) {
            logger.warn(
                    "Authority '{}' is not recognized and cannot be parsed. "
                            + "This may happen due to either manual interventions while creating a new token, "
                            + "or because of incompatible starter and master usage.",
                    name);
            return null;
        }
    }
}
