package com.nucleonforge.axile.master.domain;

import java.util.Set;

/**
 * The abstraction that holds the Java classes being loaded by this application.
 *
 * @author Mikhail Polivakha
 */
public class LoadedClasses {

    private Set<LoadedClass> loadedClass;

    public LoadedClasses(Set<LoadedClass> loadedClass) {
        this.loadedClass = loadedClass;
    }
}
