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
import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import org.jspecify.annotations.NullMarked;

import com.axelixlabs.axelix.common.auth.core.JwtAlgorithm;
import com.axelixlabs.axelix.common.auth.exception.JwtTokenGenerationException;
import com.axelixlabs.axelix.common.auth.service.JwtSigningStrategy;
import com.axelixlabs.axelix.common.auth.service.JwtSigningStrategyFactory;
import com.axelixlabs.axelix.common.contract.jwt.AxelixJwtToken;
import com.axelixlabs.axelix.common.contract.jwt.JwtRole;
import com.axelixlabs.axelix.master.domain.auth.Authority;
import com.axelixlabs.axelix.master.domain.auth.Role;
import com.axelixlabs.axelix.master.domain.auth.User;

/**
 * Service responsible for generating JWT tokens from {@link User} instances.
 *
 * @see AxelixJwtToken
 * @since 22.07.2025
 * @author Nikita Kirillov
 * @author Mikhail Polivakha
 */
@NullMarked
public class DefaultUserJwtEncoderService implements UserJwtEncoderService {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final JwtSigningStrategy signingStrategy;

    private final String signingKey;

    private final Duration lifespan;

    public DefaultUserJwtEncoderService(
            final JwtAlgorithm algorithm, final String signingKey, final Duration lifespan) {
        this.signingStrategy = JwtSigningStrategyFactory.createSigningStrategy(algorithm);
        this.signingKey = signingKey;
        this.lifespan = lifespan;
    }

    @Override
    public String generateToken(User user) throws JwtTokenGenerationException {
        return generateToken(user, lifespan);
    }

    @Override
    public String generateToken(User user, Duration lifespan) throws JwtTokenGenerationException {
        validateUser(user);
        AxelixJwtToken token =
                new AxelixJwtToken().sub(user.getUsername()).uid(user.getId()).roles(buildRoleClaims(user));
        return sign(token, lifespan);
    }

    /**
     * Signs the claims from the shared JWT contract model.
     */
    public String sign(AxelixJwtToken token, Duration lifespan) throws JwtTokenGenerationException {
        if (token.getSub() == null || token.getSub().isEmpty()) {
            throw new JwtTokenGenerationException("Username cannot be null or empty");
        }

        try {
            Instant now = Instant.now();
            token.iat(now.getEpochSecond()).exp(now.plus(lifespan).getEpochSecond());
            JwtBuilder builder = Jwts.builder().content(OBJECT_MAPPER.writeValueAsBytes(token));

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

        if (user.getUsername().isEmpty()) {
            throw new JwtTokenGenerationException("Username cannot be null or empty");
        }
    }

    private List<JwtRole> buildRoleClaims(User user) {
        return user.getRoles().stream().map(this::toJwtRole).collect(Collectors.toList());
    }

    private JwtRole toJwtRole(Role role) {
        List<String> authorities =
                role.authorities().stream().map(Authority::getName).collect(Collectors.toList());

        List<JwtRole> components =
                role.components().stream().map(this::toJwtRole).collect(Collectors.toList());

        return new JwtRole().name(role.name()).authorities(authorities).components(components);
    }
}
