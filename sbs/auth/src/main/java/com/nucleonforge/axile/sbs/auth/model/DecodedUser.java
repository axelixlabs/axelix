package com.nucleonforge.axile.sbs.auth.model;

import java.util.Set;

import com.nucleonforge.axile.common.auth.core.Role;

/**
 * The user that gets decoded from JWT token.
 *
 * @author Mikhail Polivakha
 */
public record DecodedUser(String username, Set<Role> roles) {}
