package com.nucleonforge.axile.master.auth.spi.provider;

import com.nucleonforge.axile.common.auth.core.User;

/**
 * SPI interface that is capable to load the {@link User} by his/her username.
 *
 * @since 16.07.25
 * @author Mikhail Polivakha
 */
public interface UserProvider {

    /**
     * Load user by username.
     *
     * @param username by which the user will be loaded.
     * @return the loaded {@link User}.
     * @throws UserNotFoundException in case the user is not found.
     */
    User load(String username) throws UserNotFoundException;
}
