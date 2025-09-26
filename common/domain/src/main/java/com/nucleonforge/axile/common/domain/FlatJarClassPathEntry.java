package com.nucleonforge.axile.common.domain;

/**
 * Represents a flat dependency JAR that the app relies upon.
 *
 * @since 19.08.2025
 * @author Nikita Kirillov
 */
public class FlatJarClassPathEntry implements ClassPathEntry {

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

    public FlatJarClassPathEntry(String groupId, String artifactId, String version) {
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;
    }
}
