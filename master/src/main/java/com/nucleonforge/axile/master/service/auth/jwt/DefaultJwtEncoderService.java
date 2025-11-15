package com.nucleonforge.axile.master.service.auth.jwt;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;

import com.nucleonforge.axile.common.auth.core.Authority;
import com.nucleonforge.axile.common.auth.core.JwtRole;
import com.nucleonforge.axile.common.auth.core.Role;
import com.nucleonforge.axile.common.auth.core.User;
import com.nucleonforge.axile.common.auth.spi.jwt.JwtAlgorithm;
import com.nucleonforge.axile.common.auth.spi.jwt.TokenClaim;
import com.nucleonforge.axile.master.exception.auth.JwtTokenGenerationException;

/**
 * Service responsible for generating JWT tokens from {@link User} instances.
 *
 * @see JwtRole
 * @since 22.07.2025
 * @author Nikita Kirillov
 */
public class DefaultJwtEncoderService implements JwtEncoderService {

    private final JwtSigningStrategy signingStrategy;

    private final String signingKey;

    private final Duration lifespan;

    public DefaultJwtEncoderService(final JwtAlgorithm algorithm, final String signingKey, final Duration lifespan) {
        this.signingStrategy = JwtSigningStrategyFactory.createSigningStrategy(algorithm);
        this.signingKey = signingKey;
        this.lifespan = lifespan;
    }

    @Override
    public String generateToken(User user) throws JwtTokenGenerationException {
        validateUser(user);

        try {
            Instant now = Instant.now();
            List<JwtRole> roleClaims = buildRoleClaims(user);

            JwtBuilder builder = Jwts.builder()
                    .subject(user.username())
                    .issuedAt(Date.from(now))
                    .expiration(Date.from(now.plus(lifespan)))
                    .claim(TokenClaim.ROLES.getEncoding(), roleClaims);

            return signingStrategy.signToken(builder, signingKey).compact();
        } catch (JwtTokenGenerationException e) {
            throw e;
        } catch (Exception e) {
            throw new JwtTokenGenerationException("Failed to generate JWT token", e);
        }
    }

    private void validateUser(User user) {
        if (user == null) {
            throw new JwtTokenGenerationException("User cannot be null");
        }

        if (user.username() == null || user.username().isEmpty()) {
            throw new JwtTokenGenerationException("Username cannot be null or empty");
        }
    }

    private List<JwtRole> buildRoleClaims(User user) {
        return user.roles().stream().map(this::toJwtRole).toList();
    }

    private JwtRole toJwtRole(Role role) {
        Set<String> authorities =
                role.authorities().stream().map(Authority::getName).collect(Collectors.toSet());

        List<JwtRole> components =
                role.components().stream().map(this::toJwtRole).toList();

        return new JwtRole(role.name(), authorities, components);
    }
}
