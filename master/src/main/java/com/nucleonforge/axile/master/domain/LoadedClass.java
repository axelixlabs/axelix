package com.nucleonforge.axile.master.domain;

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
     * The class-path-entry from which the given class is loaded.
     */
    private ClassPathEntry classPathEntry;

    public LoadedClass(ClassLoader classLoader, String fqcn, ClassPathEntry classPathEntry) {
        this.classLoader = classLoader;
        this.fqcn = fqcn;
        this.classPathEntry = classPathEntry;
    }
}
