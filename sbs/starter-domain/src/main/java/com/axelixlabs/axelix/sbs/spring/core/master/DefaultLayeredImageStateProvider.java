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
package com.axelixlabs.axelix.sbs.spring.core.master;

/**
 * Default implementation of {@link LayeredImageStateProvider}.
 *
 * @author Ilya Naumov
 */
public class DefaultLayeredImageStateProvider implements LayeredImageStateProvider {
    private final ContainerEnvironmentDetector containerEnvironmentDetector;
    private final LibraryLocationProvider libraryLocationProvider;

    public DefaultLayeredImageStateProvider(
        ContainerEnvironmentDetector containerEnvironmentDetector, LibraryLocationProvider libraryLocationProvider) {
        this.containerEnvironmentDetector = containerEnvironmentDetector;
        this.libraryLocationProvider = libraryLocationProvider;
    }

    /**
     * Determines whether layered OCI image processing is enabled.
     * Layered OCI images are supported only when running inside a container
     * and when the library is loaded from a file-based location.
     */
    @Override
    public boolean isLayeredImageEnabled() {
        return containerEnvironmentDetector.isRunningInContainer() && libraryLocationProvider.hasFileProtocol();
    }
}
