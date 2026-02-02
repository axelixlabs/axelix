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
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;

import com.axelixlabs.axelix.common.auth.core.Authority;
import com.axelixlabs.axelix.common.auth.core.JwtAlgorithm;
import com.axelixlabs.axelix.common.auth.core.JwtRole;
import com.axelixlabs.axelix.common.auth.core.Role;
import com.axelixlabs.axelix.common.auth.core.TokenClaim;
import com.axelixlabs.axelix.common.auth.core.User;
import com.axelixlabs.axelix.master.exception.auth.JwtTokenGenerationException;

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
                    .subject(user.getUsername())
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

        if (user.getUsername() == null || user.getUsername().isEmpty()) {
            throw new JwtTokenGenerationException("Username cannot be null or empty");
        }
    }

    private List<JwtRole> buildRoleClaims(User user) {
        return user.getRoles().stream().map(this::toJwtRole).toList();
    }

    private JwtRole toJwtRole(Role role) {
        Set<String> authorities =
                role.getAuthorities().stream().map(Authority::getName).collect(Collectors.toSet());

        List<JwtRole> components =
                role.getComponents().stream().map(this::toJwtRole).toList();

        return new JwtRole(role.getName(), authorities, components);
    }
}
