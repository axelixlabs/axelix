package com.nucleonforge.axile.master.auth.spi.jwt.singing;

import java.util.Objects;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.MacAlgorithm;

import com.nucleonforge.axile.common.auth.spi.jwt.JwtAlgorithm;

/**
 * Factory for creating {@link JwtSigningStrategy} instances
 * based on the specified {@link JwtAlgorithm}.
 *
 * @since 25.07.2025
 * @author Nikita Kirillov
 */
public class JwtSigningStrategyFactory {

    private JwtSigningStrategyFactory() {}

    @SuppressWarnings("NullAway")
    public static JwtSigningStrategy createSigningStrategy(JwtAlgorithm algorithm) {
        String algorithmName = Objects.requireNonNull(algorithm.getAlgorithmName(), "Algorithm name cannot be null");

        switch (algorithm) {
            case HMAC256, HMAC384, HMAC512 -> {
                MacAlgorithm macAlgorithm = (MacAlgorithm) Jwts.SIG.get().get(algorithmName);

                return new HmacSigningStrategy(macAlgorithm, algorithm.getMinKeyLength());
            }
            default ->
                throw new UnsupportedOperationException("Unsupported algorithm: " + algorithm.getAlgorithmName());
        }
    }
}
