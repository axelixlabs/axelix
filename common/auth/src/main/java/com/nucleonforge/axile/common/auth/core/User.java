package com.nucleonforge.axile.common.auth.core;

import java.util.Set;

/**
 * SPI interface of a User. The user, from a conceptual perspective, is
 * identified by his/her {@link #username()} and a set of {@link #roles()}
 * that are assigned to him/her.
 *
 * @since 16.07.25
 * @author Mikhail Polivakha
 */
// TODO: add jspecify annotations
public interface User {

    /**
     * Username of the given user.
     */
    String username();

    /**
     * Password of the given user.
     */
    String password();

    /**
     * Set of {@link Role roles} that are assigned to this User.
     */
    Set<Role> roles();
}
