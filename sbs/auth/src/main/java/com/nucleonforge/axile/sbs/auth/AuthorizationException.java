package com.nucleonforge.axile.sbs.auth;

/**
 * The exception that happened during the authorization
 *
 * @since 16.07.25
 * @author Mikhail Polivakha
 */
public class AuthorizationException extends RuntimeException {

    public AuthorizationException() {
        super();
    }

    public AuthorizationException(final String message) {
        super(message);
    }

    public AuthorizationException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
