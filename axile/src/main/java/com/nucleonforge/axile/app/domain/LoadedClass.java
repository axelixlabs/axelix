package com.nucleonforge.axile.app.domain;

/**
 * The link to the class that is loaded by the application.
 *
 * @since 19.07.2025
 * @author Mikhail Polivakha
 */
public class LoadedClass {

    /**
     * The link to class loader that loaded that particular class.
     */
    private ClassLoader classLoader;

    /**
     * Fully qualified class name.
     */
    private String fqcn;

    /**
     * The dependency from which the given class is loaded.
     */
    private Dependency dependency;
}
