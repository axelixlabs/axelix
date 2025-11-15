package com.nucleonforge.axile.master.autoconfiguration;

/**
 * Configuration for static-admin for the Axile Master - the username/password pair.
 *
 * @author Mikhail Polivakha
 */
@SuppressWarnings("NullAway")
public class StaticAdminCredentialsProperties {

    private String username;
    private String password;

    public StaticAdminCredentialsProperties setUsername(String username) {
        this.username = username;
        return this;
    }

    public StaticAdminCredentialsProperties setPassword(String password) {
        this.password = password;
        return this;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }
}
