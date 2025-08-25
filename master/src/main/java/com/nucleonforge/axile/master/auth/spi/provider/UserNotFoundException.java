package com.nucleonforge.axile.master.auth.spi.provider;

import com.nucleonforge.axile.common.auth.core.User;
import com.nucleonforge.axile.master.auth.AuthenticationException;

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
