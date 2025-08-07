package com.nucleonforge.axile.master.domain;

import java.util.Set;

/**
 * Holds the references to root dependencies that are used by a particular application.
 *
 * @since 19.07.2025
 * @author Mikhail Polivakha
 */
public class Dependencies {

    private Set<Dependency> dependencies;

    public Dependencies(Set<Dependency> dependencies) {
        this.dependencies = dependencies;
    }
}
