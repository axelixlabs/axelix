package com.nucleonforge.axile.common.auth.jwt.verification;

import com.nucleonforge.axile.common.auth.jwt.JwtAlgorithm;

/**
 * Factory class for creating JWT verification strategy instances based on the specified algorithm.
 *
 * <p>This factory provides a centralized way to obtain appropriate verification strategies
 * for different JWT signing algorithms.</p>
 *
 * @since 25.07.2025
 * @author Nikita Kirillov
 */
public class JwtVerificationStrategyFactory {

    private JwtVerificationStrategyFactory() {}

    public static JwtVerificationStrategy createVerificationStrategy(JwtAlgorithm algorithm) {
        return switch (algorithm) {
            case HMAC256, HMAC384, HMAC512 -> new HmacVerificationStrategy();
        };
    }
}
