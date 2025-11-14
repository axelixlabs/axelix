package com.nucleonforge.axile.master.exception.auth;

/**
 * The exception that happened during the authentication
 *
 * @since 16.07.25
 * @author Mikhail Polivakha
 */
public class AuthenticationException extends RuntimeException {

    public AuthenticationException() {
        super();
    }

    public AuthenticationException(final String message) {
        super(message);
    }

    public AuthenticationException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
