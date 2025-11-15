package com.nucleonforge.axile.master.exception.auth;

import com.nucleonforge.axile.master.service.auth.jwt.JwtEncoderService;

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
