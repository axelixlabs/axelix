package com.nucleonforge.axile.master.model.software;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * The abstract library, like Hibernate or Spring Framework. It is an abstract notion
 * i.e. it does not have a specific version or instance it is bound to.
 *
 * @see SoftwareDistribution
 * @author Mikhail Polivakha
 */
public class LibraryComponent implements SoftwareComponent {

    private final String artifactId;
    private final String groupId;
    private final String slug;

    @Nullable
    private final String description;

    boolean isCore;

    public LibraryComponent(
            @NonNull String artifactId,
            @NonNull String groupId,
            @NonNull String slug,
            @Nullable String description,
            boolean isCore) {
        this.artifactId = artifactId;
        this.groupId = groupId;
        this.slug = slug;
        this.description = description;
        this.isCore = isCore;
    }

    @Override
    public @NonNull String getName() {
        return slug;
    }

    @Override
    public @Nullable String getDescription() {
        return description;
    }

    @Override
    public boolean isCore() {
        return isCore;
    }

    public String getArtifactId() {
        return artifactId;
    }

    public String getGroupId() {
        return groupId;
    }
}
