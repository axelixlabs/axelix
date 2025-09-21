package com.nucleonforge.axile.common.auth.exception;

/**
 * Indicates that an error occurred while parsing a JWT (JSON Web Token).
 *
 * @since 26.08.2025
 * @author Nikita Kirillov
 */
public class JwtParsingException extends RuntimeException {

    public JwtParsingException(String message) {
        super(message);
    }
}
