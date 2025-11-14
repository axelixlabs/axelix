package com.nucleonforge.axile.master.service.auth.provider;

import com.nucleonforge.axile.common.auth.core.User;
import com.nucleonforge.axile.master.exception.auth.UserNotFoundException;

/**
 * {@link UserProvider} that is capable to load user from LDAP directory servers.
 *
 * @since 16.07.25
 * @author Mikhail Polivakha
 */
public class LdapUserProvider implements UserProvider {

    @Override
    public User load(String username) throws UserNotFoundException {
        throw new UnsupportedOperationException();
    }
}
