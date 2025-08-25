package com.nucleonforge.axile.master.auth.spi.jwt.service;

/**
 * The exception that happened during the JWT token generation process.
 *
 * @see JwtEncoderService
 * @since 23.07.2025
 * @author Nikita Kirillov
 */
public class JwtTokenGenerationException extends RuntimeException {

    public JwtTokenGenerationException(final String message) {
        super(message);
    }

    public JwtTokenGenerationException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
