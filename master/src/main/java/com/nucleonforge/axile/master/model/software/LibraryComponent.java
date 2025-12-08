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
