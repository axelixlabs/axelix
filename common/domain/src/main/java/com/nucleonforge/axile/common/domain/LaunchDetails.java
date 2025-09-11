package com.nucleonforge.axile.common.domain;

/**
 * The details of the way the given {@link Instance} was launched.
 *
 * @author Mikhail Polivakha
 */
public class LaunchDetails {

    private final JvmProperties jvmProperties;

    private final JvmNonStandardOptions jvmNonStandardOptions;

    public LaunchDetails(JvmProperties jvmProperties, JvmNonStandardOptions jvmNonStandardOptions) {
        this.jvmProperties = jvmProperties;
        this.jvmNonStandardOptions = jvmNonStandardOptions;
    }

    public JvmProperties getJvmProperties() {
        return jvmProperties;
    }

    public JvmNonStandardOptions getJvmNonStandardOptions() {
        return jvmNonStandardOptions;
    }
}
