package com.nucleonforge.axile.common.domain;

import java.util.Set;

/**
 * The app that is deployed, potentially in multiple instances.
 *
 * @since 19.07.2025
 * @author Mikhail Polivakha
 */
public class Application {

    /**
     * The name of the application, e.g. K8S deployment name
     */
    private String name;

    /**
     * Instances of the given application, e.g. K8S pods.
     */
    private Set<Instance> instances;

    public Application(String name, Set<Instance> instances) {
        this.name = name;
        this.instances = instances;
    }
}
