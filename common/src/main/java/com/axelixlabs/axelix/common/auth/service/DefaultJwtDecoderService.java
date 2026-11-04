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
package com.axelixlabs.axelix.common.auth.service;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.lang.Assert;
import org.jspecify.annotations.Nullable;

import com.axelixlabs.axelix.common.auth.contract.token.AccessToken;
import com.axelixlabs.axelix.common.auth.contract.token.TokenRole;
import com.axelixlabs.axelix.common.auth.core.Authority;
import com.axelixlabs.axelix.common.auth.core.DefaultRole;
import com.axelixlabs.axelix.common.auth.core.InternalAuthorities;
import com.axelixlabs.axelix.common.auth.core.JwtAlgorithm;
import com.axelixlabs.axelix.common.auth.core.PasswordlessUser;
import com.axelixlabs.axelix.common.auth.core.Role;
import com.axelixlabs.axelix.common.auth.exception.ExpiredJwtTokenException;
import com.axelixlabs.axelix.common.auth.exception.InvalidJwtTokenException;
import com.axelixlabs.axelix.common.auth.exception.JwtParsingException;

/**
 * Default implementation of {@link JwtDecoderService}.
 *
 * @since 22.07.2025
 * @author Nikita Kirillov
 * @author Mikhail Polivakha
 */
public class DefaultJwtDecoderService implements JwtDecoderService {

    private final JwtVerificationStrategy verificationStrategy;
    private final AuthoritiesManager authoritiesManager;
    private final String signingKey;

    public DefaultJwtDecoderService(AuthoritiesManager authoritiesManager, JwtAlgorithm algorithm, String signingKey) {
        Assert.notNull(algorithm, "The jwt signing algorithm is not specified, although it is required");
        Assert.notNull(signingKey, "The jwt signing key is not specified, although it is required");

        this.authoritiesManager = authoritiesManager;
        this.verificationStrategy = JwtVerificationStrategyFactory.createVerificationStrategy(algorithm);
        this.signingKey = Objects.requireNonNull(signingKey);
    }

    @Override
    public PasswordlessUser decodeTokenToUser(String token)
            throws ExpiredJwtTokenException, InvalidJwtTokenException, JwtParsingException {

        try {
            // Signature and expiry are checked by the JWT library. The claim object itself is the
            // generated contract, read back with Jackson from the same JSON bytes that were signed.
            verificationStrategy.verifyAndParse(token, signingKey);
            AccessToken payload = AccessTokenJson.read(payloadJson(token));
            return new PasswordlessUser(
                    extractUserId(payload), extractSubject(payload), extractRoles(payload.getRoles()));
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

    private static String payloadJson(String token) {
        String[] parts = token.split("\\.");
        if (parts.length < 2) {
            throw new JwtParsingException("JWT token is invalid or tampered");
        }
        try {
            return new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);
        } catch (IllegalArgumentException e) {
            throw new JwtParsingException("JWT token is invalid or tampered", e);
        }
    }

    private String extractUserId(AccessToken payload) {
        return Optional.ofNullable(payload.getUid())
                .orElseThrow(() -> new JwtParsingException("'uid' claim is required in the incoming JWT token"));
    }

    private String extractSubject(AccessToken payload) {
        return Optional.ofNullable(payload.getSub())
                .orElseThrow(() -> new JwtParsingException("'sub' claim is required in the incoming JWT token"));
    }

    private Set<Role> extractRoles(@Nullable List<TokenRole> roles) {
        return Optional.ofNullable(roles).orElse(List.of()).stream()
                .map(this::mapToRole)
                .collect(Collectors.toSet());
    }

    private Role mapToRole(TokenRole tokenRole) {
        String roleName = tokenRole.getName();

        if (roleName == null) {
            throw new JwtParsingException("Role name is null in JWT token");
        }

        List<String> authoritiesList =
                Optional.ofNullable(tokenRole.getAuthorities()).orElse(List.of());

        Set<Authority> authorities = authoritiesList.stream()
                .map(this::parseAuthority)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        List<TokenRole> components = Optional.ofNullable(tokenRole.getComponents()).orElse(List.of());

        Set<Role> componentRoles = components.stream().map(this::mapToRole).collect(Collectors.toSet());

        return new DefaultRole(roleName, authorities, componentRoles);
    }

    private @Nullable Authority parseAuthority(String it) {
        return Optional.ofNullable(authoritiesManager.decode(it)).orElseGet(() -> {
            // well, this is not really clear that we need some complicated processing for internal authorities yet,
            // so it is implemented in this way. We may in the future revisit the AuthoritiesAccessor or introduce
            // some abstraction that is capable to resolve Internal Authorities, but as of now, I think, it is a bit
            // redundant
            if (InternalAuthorities.HEART_BEATING_AUTHORITY.getName().equals(it)) {
                return InternalAuthorities.HEART_BEATING_AUTHORITY;
            } else {
                return null;
            }
        });
    }
}
