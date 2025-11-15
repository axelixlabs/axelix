package com.nucleonforge.axile.sbs.auth.spi;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import com.nucleonforge.axile.common.auth.core.Authority;
import com.nucleonforge.axile.common.auth.core.AuthorizationRequest;
import com.nucleonforge.axile.common.auth.core.Role;
import com.nucleonforge.axile.sbs.auth.AuthorizationException;
import com.nucleonforge.axile.sbs.auth.model.DecodedUser;

/**
 * Default implementation of {@link Authorizer}.
 *
 * @since 30.07.25
 * @author Nikita Kirillov
 */
public class DefaultAuthorizer implements Authorizer {

    @Override
    public void authorize(DecodedUser user, AuthorizationRequest authorizationRequest) throws AuthorizationException {
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
            throw new AuthorizationException("Access denied: missing required authorities " + requiredNames);
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
