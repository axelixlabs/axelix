package com.axelixlabs.axelix.master.service.auth.provider;

import java.util.List;

import com.axelixlabs.axelix.common.auth.core.User;
import org.jspecify.annotations.Nullable;

/**
 * Composite {@link UserAuthenticator}.
 *
 * @author Mikhail Polivakha
 */
public class CompositeUserAuthenticator implements UserAuthenticator {

    private final List<UserAuthenticator> userAuthenticators;

    public CompositeUserAuthenticator(List<UserAuthenticator> userAuthenticators) {
        this.userAuthenticators = userAuthenticators;
    }

    @Override
    public @Nullable User authenticate(String username, String password) {
        for (var userAuthenticator : userAuthenticators) {
            User authenticatedUser = userAuthenticator.authenticate(username, password);

            if (authenticatedUser != null) {
                return authenticatedUser;
            }
        }

        return null;
    }
}
