package com.nucleonforge.axile.master.service.auth.jwt;

import io.jsonwebtoken.JwtBuilder;

import com.nucleonforge.axile.master.exception.auth.JwtTokenGenerationException;

/**
 * Strategy interface for signing JWT tokens with a specific signing key.
 *
 * <p>Implementations of this interface are responsible for applying digital signatures
 * to JWT tokens using cryptographic algorithms and key types.</p>
 *
 * @since 25.07.2025
 * @author Nikita Kirillov
 */
public interface JwtSigningStrategy {

    /**
     * Applies a digital signature to the JWT token using the provided signing key.
     *
     * @param builder the JWT builder to apply the signature to
     * @param signingKey the key used for signing the token
     * @return the signed JwtBuilder instance
     * @throws JwtTokenGenerationException if signing fails due to invalid key,
     *         weak key, or other cryptographic issues
     */
    JwtBuilder signToken(JwtBuilder builder, String signingKey) throws JwtTokenGenerationException;
}
