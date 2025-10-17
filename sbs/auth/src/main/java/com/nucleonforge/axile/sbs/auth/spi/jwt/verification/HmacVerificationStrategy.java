package com.nucleonforge.axile.sbs.auth.spi.jwt.verification;

import java.nio.charset.StandardCharsets;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

/**
 * {@link JwtVerificationStrategy} implementation that verifies and parses JWT tokens
 * using HMAC-SHA signing algorithms.
 *
 * @since 25.07.2025
 * @author Nikita Kirillov
 */
public class HmacVerificationStrategy implements JwtVerificationStrategy {

    @Override
    public Jws<Claims> verifyAndParse(String token, String signingKey) throws JwtException {
        return Jwts.parser()
                .verifyWith(Keys.hmacShaKeyFor(signingKey.getBytes(StandardCharsets.UTF_8)))
                .build()
                .parseSignedClaims(token);
    }
}
