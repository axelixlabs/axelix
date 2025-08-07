package com.nucleonforge.axile.auth.spi.provider;

import com.nucleonforge.axile.auth.AuthenticationException;
import com.nucleonforge.axile.common.auth.core.User;

/**
 * Thrown in case the {@link User} is not found by {@link UserProvider}.
 *
 * @see UserProvider
 * @since 16.07.25
 * @author Mikhail Polivakha
 */
public class UserNotFoundException extends AuthenticationException {

    public UserNotFoundException(final String message) {
        super(message);
    }

    public UserNotFoundException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
