package com.nucleonforge.axile.common.auth.exception;

import com.nucleonforge.axile.common.auth.jwt.service.JwtDecoderService;

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
