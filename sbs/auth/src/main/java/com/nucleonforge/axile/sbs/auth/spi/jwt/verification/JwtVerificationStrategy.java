package com.nucleonforge.axile.sbs.auth.spi.jwt.verification;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;

/**
 * Strategy interface for verifying and parsing JWT tokens with a specific signing key.
 *
 * <p>Implementations of this interface are responsible for validating JWT tokens,
 * verifying their digital signatures, and returning the parsed claims if verification succeeds.</p>
 *
 * @since 25.07.2025
 * @author Nikita Kirillov
 */
public interface JwtVerificationStrategy {

    /**
     * Verifies and parses a JWT token using the provided signing key.
     *
     * @param token the JWT token to verify and parse
     * @param signingKey the key used to verify the token's signature
     * @return verified JWT claims in JWS format
     * @throws JwtException if verification fails (invalid signature, expired token, etc.)
     * @throws IllegalArgumentException if the token or key is malformed
     */
    Jws<Claims> verifyAndParse(String token, String signingKey) throws JwtException;
}
