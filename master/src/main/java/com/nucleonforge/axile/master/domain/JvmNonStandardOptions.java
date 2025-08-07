package com.nucleonforge.axile.master.domain;

import java.util.Set;

/**
 * Handle for {@link JvmNonStandardOption JVM non standard options}.
 *
 * @author Mikhail Polivakha
 */
public class JvmNonStandardOptions {

    private Set<JvmNonStandardOption> nonStandardOptions;

    public JvmNonStandardOptions(Set<JvmNonStandardOption> nonStandardOptions) {
        this.nonStandardOptions = nonStandardOptions;
    }
}
