package com.axelixlabs.axelix.common.auth.core;

public class DefaultAuthority implements Authority {

    private final String codename;

    public DefaultAuthority(String codename) {
        this.codename = codename;
    }

    @Override
    public String getName() {
        return "";
    }
}
