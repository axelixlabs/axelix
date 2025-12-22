/*
 * Copyright 2025-present, Nucleon Forge Software.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.nucleonforge.axile.common.auth.rbac.jwt.service;

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

import com.nucleonforge.axile.common.auth.JwtAlgorithm;
import com.nucleonforge.axile.common.auth.exception.ExpiredJwtTokenException;
import com.nucleonforge.axile.common.auth.exception.InvalidJwtTokenException;
import com.nucleonforge.axile.common.auth.exception.JwtParsingException;
import com.nucleonforge.axile.common.auth.exception.JwtTokenDecodingException;
import com.nucleonforge.axile.common.auth.rbac.core.Authority;
import com.nucleonforge.axile.common.auth.rbac.core.DefaultAuthority;
import com.nucleonforge.axile.common.auth.rbac.core.DefaultRole;
import com.nucleonforge.axile.common.auth.rbac.core.Role;
import com.nucleonforge.axile.common.auth.rbac.jwt.TokenClaim;
import com.nucleonforge.axile.common.auth.rbac.jwt.model.DecodedUser;
import com.nucleonforge.axile.common.auth.rbac.jwt.verification.RbacJwtVerificationStrategy;
import com.nucleonforge.axile.common.auth.rbac.jwt.verification.RbacJwtVerificationStrategyFactory;

/**
 * Default implementation of {@link RbacJwtDecoderService}.
 *
 * @since 22.07.2025
 * @author Nikita Kirillov
 */
public class DefaultRbacJwtDecoderService implements RbacJwtDecoderService {

    private static final Logger logger = LoggerFactory.getLogger(DefaultRbacJwtDecoderService.class);

    private final RbacJwtVerificationStrategy verificationStrategy;

    private final String signingKey;

    public DefaultRbacJwtDecoderService(JwtAlgorithm algorithm, String signingKey) {
        this.verificationStrategy = RbacJwtVerificationStrategyFactory.createVerificationStrategy(algorithm);
        this.signingKey = Objects.requireNonNull(signingKey);
    }

    @Override
    public DecodedUser decodeTokenToUser(String token)
            throws ExpiredJwtTokenException, InvalidJwtTokenException, JwtTokenDecodingException, JwtParsingException {

        try {
            Claims claims = parseClaims(token).getPayload();
            return new DecodedUser(claims.getSubject(), extractRoles(claims));
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
