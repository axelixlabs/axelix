package com.nucleonforge.axile.app.domain;

/**
 * Represents a dependency JAR that the app relies upon.
 *
 * @since 19.07.2025
 * @author Mikhail Polivakha
 */
public class Dependency {

    /**
     * Group ID of the dependency
     */
    private String groupId;

    /**
     * Artifact ID of the dependency
     */
    private String artifactId;

    /**
     * Version of the dependency
     */
    private String version;

    /**
     * In case this dependency is transitive, the link to dependency that brought
     * this dependency.
     * <p>
     * Might be null
     */
    private Dependency broughtBy;

    /**
     * List of dependencies upon which dependency depends upon.
     * <p>
     * Might be empty, never null
     */
    private Set<Dependency> dependsOn;
}
