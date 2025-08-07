package com.nucleonforge.axile.master.domain;

/**
 * The details of the way the given {@link Instance} was launched.
 *
 * @author Mikhail Polivakha
 */
public class LaunchDetails {

    private JvmProperties jvmProperties;

    private JvmNonStandardOptions jvmNonStandardOptions;

    public LaunchDetails(JvmProperties jvmProperties, JvmNonStandardOptions jvmNonStandardOptions) {
        this.jvmProperties = jvmProperties;
        this.jvmNonStandardOptions = jvmNonStandardOptions;
    }
}
