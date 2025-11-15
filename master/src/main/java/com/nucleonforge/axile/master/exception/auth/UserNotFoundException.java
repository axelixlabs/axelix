package com.nucleonforge.axile.master.exception.auth;

import com.nucleonforge.axile.common.auth.core.User;
import com.nucleonforge.axile.master.service.auth.provider.UserProvider;

/**
 * Thrown in case the {@link User} is not found by {@link UserProvider}.
 *
 * @see UserProvider
 * @since 16.07.25
 * @author Mikhail Polivakha
 */
public class UserNotFoundException extends AuthenticationException {

    public UserNotFoundException(final String username) {
        super("User with username '%s' is not found".formatted(username));
    }

    public UserNotFoundException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
