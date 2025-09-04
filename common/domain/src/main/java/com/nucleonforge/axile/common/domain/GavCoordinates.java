package com.nucleonforge.axile.common.domain;

import org.jspecify.annotations.NonNull;

/**
 * The GAV coordinates of the dependency.
 *
 * @param groupId Group ID of the dependency.
 * @param artifactId Artifact ID of the dependency.
 * @param version Version of the dependency.
 *
 * @author Mikhail Polivakha
 */
public record GavCoordinates(@NonNull String groupId, @NonNull String artifactId, @NonNull String version) {}
