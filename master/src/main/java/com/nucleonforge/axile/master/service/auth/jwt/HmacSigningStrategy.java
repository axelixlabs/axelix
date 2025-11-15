package com.nucleonforge.axile.master.service.auth.jwt;

import java.nio.charset.StandardCharsets;

import javax.crypto.SecretKey;

import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.MacAlgorithm;
import io.jsonwebtoken.security.WeakKeyException;

import com.nucleonforge.axile.master.exception.auth.JwtTokenGenerationException;

/**
 * {@link JwtSigningStrategy} implementation that signs JWT tokens using HMAC-SHA algorithms.
 *
 * @since 25.07.2025
 * @author Nikita Kirillov
 */
public class HmacSigningStrategy implements JwtSigningStrategy {

    private final MacAlgorithm algorithm;
    private final int minKeyLength;

    public HmacSigningStrategy(MacAlgorithm algorithm, int minKeyLength) {
        this.algorithm = algorithm;
        this.minKeyLength = minKeyLength;
    }

    @Override
    public JwtBuilder signToken(JwtBuilder builder, String signingKey) throws JwtTokenGenerationException {
        try {
            SecretKey key = Keys.hmacShaKeyFor(signingKey.getBytes(StandardCharsets.UTF_8));
            return builder.signWith(key, algorithm);
        } catch (WeakKeyException e) {
            throw new JwtTokenGenerationException(
                    "The secret key is too weak for " + algorithm + " algorithm. " + "It must be at least "
                            + minKeyLength + " bytes.",
                    e);
        } catch (Exception e) {
            throw new JwtTokenGenerationException("Failed to sign JWT token", e);
        }
    }
}
