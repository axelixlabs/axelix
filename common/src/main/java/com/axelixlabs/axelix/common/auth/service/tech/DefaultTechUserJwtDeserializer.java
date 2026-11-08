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

import com.axelixlabs.axelix.common.auth.service.JwtVerificationStrategy;
import com.axelixlabs.axelix.common.auth.service.JwtVerificationStrategyFactory;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import org.jspecify.annotations.NullMarked;

import com.axelixlabs.axelix.common.auth.core.JwtAlgorithm;
import com.axelixlabs.axelix.common.auth.core.TechUser;
import com.axelixlabs.axelix.common.auth.exception.InvalidJwtTokenException;
import com.axelixlabs.axelix.common.auth.exception.JwtParsingException;

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
    public TechUser deserialize(String token) throws InvalidJwtTokenException, JwtParsingException {
        try {
            Claims claims =
                    verificationStrategy.verifyAndParse(token, signingKey).getPayload();
            return resolveTechUser(claims.getSubject());
        } catch (JwtParsingException e) {
            throw e;
        } catch (JwtException e) {
            throw new InvalidJwtTokenException("JWT token is invalid or tampered", e);
        } catch (Exception e) {
            throw new JwtParsingException("Unexpected error while decoding technical user JWT token", e);
        }
    }

    private TechUser resolveTechUser(String subject) {
        for (TechUser techUser : TechUser.values()) {
            if (techUser.getId().equals(subject)) {
                return techUser;
            }
        }
        throw new JwtParsingException("Unknown technical user in JWT token subject");
    }
}
