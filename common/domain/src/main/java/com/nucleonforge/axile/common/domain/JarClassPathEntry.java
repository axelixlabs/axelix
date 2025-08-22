package com.nucleonforge.axile.common.domain;

import java.util.Set;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * Represents a dependency JAR that the app relies upon.
 *
 * @since 19.07.2025
 * @author Mikhail Polivakha
 */
public class JarClassPathEntry implements ClassPathEntry {

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
    @Nullable
    private JarClassPathEntry broughtBy;

    /**
     * List of dependencies upon which dependency depends upon.
     * <p>
     * Might be empty, never null
     */
    @NonNull
    private Set<JarClassPathEntry> dependsOn;

    public JarClassPathEntry(
            String groupId,
            String artifactId,
            String version,
            @Nullable JarClassPathEntry broughtBy,
            @NonNull Set<JarClassPathEntry> dependsOn) {
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;
        this.broughtBy = broughtBy;
        this.dependsOn = dependsOn;
    }
}
