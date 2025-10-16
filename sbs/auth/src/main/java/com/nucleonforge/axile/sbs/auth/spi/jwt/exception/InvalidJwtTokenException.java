package com.nucleonforge.axile.sbs.auth.spi.jwt.exception;

import com.nucleonforge.axile.sbs.auth.spi.jwt.service.JwtDecoderService;

/**
 * Indicates that a provided JWT token is invalid.
 *
 * <p>This may occur due to tampering, incorrect signature, or structural issues in the token.</p>
 *
 * @see JwtDecoderService
 * @since 23.07.2025
 * @author Nikita Kirillov
 */
public class InvalidJwtTokenException extends RuntimeException {

    public InvalidJwtTokenException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
