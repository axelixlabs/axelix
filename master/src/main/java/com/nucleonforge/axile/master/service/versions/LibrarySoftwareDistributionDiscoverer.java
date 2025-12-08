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
package com.nucleonforge.axile.master.service.versions;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import com.nucleonforge.axile.common.domain.BuildInfo;
import com.nucleonforge.axile.common.domain.ClassPathEntry;
import com.nucleonforge.axile.common.domain.JarClassPathEntry;
import com.nucleonforge.axile.master.model.software.LibraryComponent;
import com.nucleonforge.axile.master.model.software.SoftwareDistribution;

/**
 * @author Mikhail Polivakha
 */
public abstract class LibrarySoftwareDistributionDiscoverer<T extends LibraryComponent>
        implements SoftwareDistributionDiscoverer<T> {

    private final T lib;

    protected LibrarySoftwareDistributionDiscoverer(@NonNull T lib) {
        this.lib = lib;
    }

    @Override
    public @Nullable SoftwareDistribution discover(@NonNull BuildInfo buildInfo) {
        for (ClassPathEntry classPathEntry : buildInfo.getClassPath()) {

            if (classPathEntry instanceof JarClassPathEntry jar) {

                if (jar.getArtifactId().equals(lib.getArtifactId())
                        && jar.getGroupId().equals(lib.getGroupId())) {
                    return new SoftwareDistribution(lib, jar.getVersion());
                }
            }
        }

        return null;
    }
}
