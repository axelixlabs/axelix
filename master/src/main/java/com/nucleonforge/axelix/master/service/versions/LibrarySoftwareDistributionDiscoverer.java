/*
 * Copyright (C) 2025-2026 Axelix Labs
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package com.nucleonforge.axelix.master.service.versions;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import com.nucleonforge.axelix.common.domain.BuildInfo;
import com.nucleonforge.axelix.common.domain.ClassPathEntry;
import com.nucleonforge.axelix.common.domain.JarClassPathEntry;
import com.nucleonforge.axelix.master.model.software.LibraryComponent;
import com.nucleonforge.axelix.master.model.software.SoftwareDistribution;

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
