package com.nucleonforge.axile.master.api.iam.response;

import java.util.Set;

/**
 * Response to the User profile request
 *
 * @author Mikhail Polivakha
 */
public record UserProfileResponse(String username, Roles roles) {

    public record Roles(Set<Role> roles) {}

    public record Role(String name, Authorities authorities) {}

    public record Authorities(Set<Authority> authorities) {}

    public record Authority(String name) {}
}
