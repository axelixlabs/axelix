/*
 * Copyright 2025-present, Nucleon Forge Software.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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

    @NonNull
    private final GavCoordinates gavCoordinates;

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
        this.gavCoordinates = new GavCoordinates(groupId, artifactId, version);
        this.broughtBy = broughtBy;
        this.dependsOn = dependsOn;
    }

    public String getGroupId() {
        return gavCoordinates.groupId();
    }

    public String getArtifactId() {
        return gavCoordinates.artifactId();
    }

    public String getVersion() {
        return gavCoordinates.version();
    }

    public @Nullable JarClassPathEntry getBroughtBy() {
        return broughtBy;
    }

    public JarClassPathEntry setBroughtBy(@Nullable JarClassPathEntry broughtBy) {
        this.broughtBy = broughtBy;
        return this;
    }

    public @NonNull Set<JarClassPathEntry> getDependsOn() {
        return dependsOn;
    }

    public JarClassPathEntry setDependsOn(@NonNull Set<JarClassPathEntry> dependsOn) {
        this.dependsOn = dependsOn;
        return this;
    }
}
