package com.nucleonforge.axile.master.service.auth.provider;

import java.util.Objects;
import java.util.Set;

import com.nucleonforge.axile.common.auth.core.DefaultUser;
import com.nucleonforge.axile.common.auth.core.User;
import com.nucleonforge.axile.master.autoconfiguration.auth.StaticAdminCredentialsProperties;
import com.nucleonforge.axile.master.exception.auth.UserNotFoundException;
import com.nucleonforge.axile.master.service.auth.UserLoginService;

/**
 * {@link UserLoginService} that authenticates a given user by the static pair of the username/password.
 *
 * @author Mikhail Polivakha
 */
public class StaticAdminUserProvider implements UserProvider {

    private final StaticAdminCredentialsProperties staticCredentialsConfig;

    public StaticAdminUserProvider(StaticAdminCredentialsProperties staticCredentialsConfig) {
        this.staticCredentialsConfig = staticCredentialsConfig;
    }

    @Override
    public User load(String username) throws UserNotFoundException {
        if (Objects.equals(staticCredentialsConfig.getUsername(), username)) {
            // TODO: We need to revisit our roles API here.
            return new DefaultUser(username, staticCredentialsConfig.getPassword(), Set.of());
        } else {
            throw new UserNotFoundException(username);
        }
    }
}
