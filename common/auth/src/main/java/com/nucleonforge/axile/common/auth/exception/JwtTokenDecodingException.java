package com.nucleonforge.axile.common.auth.exception;

import com.nucleonforge.axile.common.auth.jwt.service.JwtDecoderService;

/**
 * Thrown when decoding or parsing a JWT token fails.
 *
 * @see JwtDecoderService
 * @since 23.07.2025
 * @author Nikita Kirillov
 */
public class JwtTokenDecodingException extends RuntimeException {

    public JwtTokenDecodingException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
