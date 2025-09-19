package com.nucleonforge.axile.common.domain;

/**
 * Contains information about the way a given application instance was launched.
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
