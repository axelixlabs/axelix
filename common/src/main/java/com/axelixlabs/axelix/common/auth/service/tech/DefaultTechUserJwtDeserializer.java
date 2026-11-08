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
package com.axelixlabs.axelix.common.auth.service.tech;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import org.jspecify.annotations.NullMarked;

import com.axelixlabs.axelix.common.auth.core.JwtAlgorithm;
import com.axelixlabs.axelix.common.auth.core.TechUser;
import com.axelixlabs.axelix.common.auth.exception.ExpiredJwtTokenException;
import com.axelixlabs.axelix.common.auth.exception.InvalidJwtTokenException;
import com.axelixlabs.axelix.common.auth.exception.JwtParsingException;
import com.axelixlabs.axelix.common.auth.service.JwtVerificationStrategy;
import com.axelixlabs.axelix.common.auth.service.JwtVerificationStrategyFactory;

/**
 * Default {@link TechUserJwtDeserializer} implementation.
 *
 * @author Mikhail Polivakha
 */
@NullMarked
public class DefaultTechUserJwtDeserializer implements TechUserJwtDeserializer {

    private final JwtVerificationStrategy verificationStrategy;

    private final String signingKey;

    public DefaultTechUserJwtDeserializer(JwtAlgorithm algorithm, String signingKey) {
        this.verificationStrategy = JwtVerificationStrategyFactory.createVerificationStrategy(algorithm);
        this.signingKey = signingKey;
    }

    @Override
    public TechUser deserialize(String token)
            throws InvalidJwtTokenException, JwtParsingException, ExpiredJwtTokenException {
        try {
            Claims claims =
                    verificationStrategy.verifyAndParse(token, signingKey).getPayload();
            if (claims.getIssuedAt() == null || claims.getExpiration() == null) {
                throw new JwtParsingException("Technical-user JWT must contain issued-at and expiration claims");
            }
            return resolveTechUser(claims.getSubject());
        } catch (JwtParsingException e) {
            throw e;
        } catch (ExpiredJwtException e) {
            throw new ExpiredJwtTokenException("JWT token has expired", e);
        } catch (JwtException e) {
            throw new InvalidJwtTokenException("JWT token is invalid or tampered", e);
        } catch (Exception e) {
            throw new JwtParsingException("Unexpected error while decoding technical user JWT token", e);
        }
    }

    /*
     * JWT parsing, signature verification and validation of the required issued-at and expiry claims happen before this method.
     * The subject identifies the technical user; additional legacy claims such as uid and roles are ignored.
     * This is intentional: initial discovery uses a legacy-format master token because the starter version
     * is not yet known. That token must be accepted by both older starters and starters from 1.3 onward.
     */
    private TechUser resolveTechUser(String subject) {
        for (TechUser techUser : TechUser.values()) {
            if (techUser.getId().equals(subject)) {
                return techUser;
            }
        }
        throw new JwtParsingException("Unknown technical user in JWT token subject");
    }
}
