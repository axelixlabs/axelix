package com.nucleonforge.axile.common.domain;

import java.util.Set;

/**
 * The abstraction that holds the Java classes being loaded by this application.
 *
 * @author Mikhail Polivakha
 */
public class LoadedClasses {

    private final Set<LoadedClass> loadedClass;

    public LoadedClasses(Set<LoadedClass> loadedClass) {
        this.loadedClass = loadedClass;
    }

    public Set<LoadedClass> getLoadedClass() {
        return loadedClass;
    }
}
