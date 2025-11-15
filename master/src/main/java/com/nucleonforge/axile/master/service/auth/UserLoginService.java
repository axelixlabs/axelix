package com.nucleonforge.axile.master.service.auth;

import com.nucleonforge.axile.master.exception.auth.AuthenticationException;

/**
 * Service that is capable to log in the user.
 *
 * @author Mikhail Polivakha
 */
public interface UserLoginService {

    /**
     * Log in the user by the specified pair of username and password.
     *
     * @throws AuthenticationException in case of authentication failure
     */
    String login(String username, String password) throws AuthenticationException;
}
