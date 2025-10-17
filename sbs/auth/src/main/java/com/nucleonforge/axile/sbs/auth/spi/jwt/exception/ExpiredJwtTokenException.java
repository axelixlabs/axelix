package com.nucleonforge.axile.sbs.auth.spi.jwt.exception;

import com.nucleonforge.axile.sbs.auth.spi.jwt.service.JwtDecoderService;

/**
 * Indicates that the JWT token has expired and can no longer be used.
 *
 * @see JwtDecoderService
 * @since 23.07.2025
 * @author Nikita Kirillov
 */
public class ExpiredJwtTokenException extends RuntimeException {

    public ExpiredJwtTokenException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
