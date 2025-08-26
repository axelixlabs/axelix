package com.nucleonforge.axile.common.domain;

import java.util.Set;

/**
 * Handle for {@link JvmNonStandardOption JVM non standard options}.
 *
 * @author Mikhail Polivakha
 */
public class JvmNonStandardOptions {

    private final Set<JvmNonStandardOption> nonStandardOptions;

    public JvmNonStandardOptions(Set<JvmNonStandardOption> nonStandardOptions) {
        this.nonStandardOptions = nonStandardOptions;
    }

    public Set<JvmNonStandardOption> getNonStandardOptions() {
        return nonStandardOptions;
    }
}
