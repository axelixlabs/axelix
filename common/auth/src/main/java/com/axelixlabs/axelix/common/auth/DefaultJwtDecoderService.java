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
import io.jsonwebtoken.lang.Assert;
import org.jspecify.annotations.Nullable;

import com.axelixlabs.axelix.common.auth.core.Authority;
import com.axelixlabs.axelix.common.auth.core.DecodedUser;
import com.axelixlabs.axelix.common.auth.core.DefaultRole;
import com.axelixlabs.axelix.common.auth.core.ExternalAuthority;
import com.axelixlabs.axelix.common.auth.core.JwtAlgorithm;
import com.axelixlabs.axelix.common.auth.core.Role;
import com.axelixlabs.axelix.common.auth.core.TokenClaim;
import com.axelixlabs.axelix.common.auth.exception.ExpiredJwtTokenException;
import com.axelixlabs.axelix.common.auth.exception.InvalidJwtTokenException;
import com.axelixlabs.axelix.common.auth.exception.JwtParsingException;

/**
 * Default implementation of {@link JwtDecoderService}.
 *
 * @since 22.07.2025
 * @author Nikita Kirillov
 */
public class DefaultJwtDecoderService implements JwtDecoderService {

    private final JwtVerificationStrategy verificationStrategy;

    private final String signingKey;

    public DefaultJwtDecoderService(JwtAlgorithm algorithm, String signingKey) {
        Assert.notNull(algorithm, "The jwt signing algorithm is not specified, although it is required");
        Assert.notNull(signingKey, "The jwt signing key is not specified, although it is required");

        this.verificationStrategy = JwtVerificationStrategyFactory.createVerificationStrategy(algorithm);
        this.signingKey = Objects.requireNonNull(signingKey);
    }

    @Override
    public DecodedUser decodeTokenToUser(String token)
            throws ExpiredJwtTokenException, InvalidJwtTokenException, JwtParsingException {

        try {
            Claims claims = parseClaims(token).getPayload();
            return new DecodedUser(claims.getSubject(), extractRoles(claims));
        } catch (JwtParsingException e) {
            throw new JwtParsingException(e);
        } catch (ExpiredJwtException e) {
            throw new ExpiredJwtTokenException("JWT token has expired", e);
        } catch (JwtException e) {
            throw new InvalidJwtTokenException("JWT token is invalid or tampered", e);
        } catch (Exception e) {
            throw new JwtParsingException("Unexpected error while decoding JWT token", e);
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
    private ExternalAuthority safeAuthoritiesFromString(String name) {
        try {
            return ExternalAuthority.valueOf(name);
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }
}
