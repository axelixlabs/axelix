package com.nucleonforge.axile.master.auth.spi.provider;

import com.nucleonforge.axile.common.auth.core.User;

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
