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
 * Detects whether the application is launched by Spring Boot {@code JarLauncher}.
 *
 * @author Ilya Naumov
 */
public interface JarLauncherDetector {
    /**
     * Determines whether the current thread's context ClassLoader or any of its
     * parents matches a ClassLoader used by {@code JarLauncher}.
     *
     * @return {@code true} if a matching ClassLoader is found in the hierarchy
     */
    boolean isThreadContextClassLoaderHierarchyMatching();

    /**
     * Determines whether a library class loader matches a ClassLoader used
     * by {@code JarLauncher}.
     *
     * @return {@code true} if a library class loader matches
     */
    boolean isLibraryClassLoaderMatching();
}
