package com.nucleonforge.axile.master.auth.spi;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nucleonforge.axile.common.auth.core.Authority;
import com.nucleonforge.axile.common.auth.core.AuthorizationRequest;
import com.nucleonforge.axile.common.auth.core.Role;
import com.nucleonforge.axile.common.auth.core.User;

/**
 * Default implementation of {@link Authorizer}.
 *
 * @since 30.07.25
 * @author Nikita Kirillov
 */
public class DefaultAuthorizer implements Authorizer {
    private static final Logger logger = LoggerFactory.getLogger(DefaultAuthorizer.class);

    @Override
    public void authorize(User user, AuthorizationRequest authorizationRequest) {
        Set<Authority> requiredAuthorities = authorizationRequest.requiredAuthorities();

        if (requiredAuthorities.isEmpty()) {
            return;
        }

        Set<String> userAuthorities = user.roles().stream()
                .flatMap(role -> collectAuthorities(role).stream())
                .map(Authority::getName)
                .collect(Collectors.toSet());

        Set<String> requiredNames =
                requiredAuthorities.stream().map(Authority::getName).collect(Collectors.toSet());

        if (!userAuthorities.containsAll(requiredNames)) {
            logger.warn(
                    "Authority '{}' is not recognized and cannot be parsed. "
                            + "This may happen due to either manual interventions with token creation, "
                            + "or because of incompatible starter and master usage.",
                    requiredNames);
        }
    }

    private Set<Authority> collectAuthorities(Role role) {
        Set<Authority> all = new HashSet<>(role.authorities());

        for (Role component : role.components()) {
            all.addAll(collectAuthorities(component));
        }

        return all;
    }
}
