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
 * Default implementation of {@link JarLauncherDetector}.
 *
 * @author Ilya Naumov
 */
public class DefaultJarLauncherDetector implements JarLauncherDetector {
    private final Class<?> libraryClass;

    private static final String LAUNCHED_URL_CLASS_LOADER_PREFIX = "org.springframework.boot.loader";
    /**
     * Spring Boot 2.0.0 - 3.1.x: {@code org.springframework.boot.loader.LaunchedURLClassLoader}
     */
    private static final String LAUNCHED_URL_CLASS_LOADER_SUFFIX = "LaunchedURLClassLoader";
    /**
     * Spring Boot 3.2.0+: {@code org.springframework.boot.loader.launch.LaunchedClassLoader}
     */
    private static final String LAUNCHED_CLASS_LOADER_SUFFIX = "LaunchedClassLoader";

    public DefaultJarLauncherDetector(Class<?> libraryClass) {
        this.libraryClass = libraryClass;
    }

    /**
     * Walks up the thread's context ClassLoader hierarchy and checks if any
     * ClassLoader name matches the Spring Boot {@code JarLauncher} ClassLoader
     * pattern.
     */
    @Override
    public boolean isThreadContextClassLoaderHierarchyMatching() {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        while (classLoader != null) {
            String name = classLoader.getClass().getName();
            if (isClassLoaderNameMatches(name)) {
                return true;
            }
            classLoader = classLoader.getParent();
        }
        return false;
    }

    /**
     * Checks if the ClassLoader that loaded the configured library class matches
     * the Spring Boot {@code JarLauncher} ClassLoader pattern.
     */
    @Override
    public boolean isLibraryClassLoaderMatching() {
        ClassLoader classLoader = libraryClass.getClassLoader();
        if (classLoader == null) {
            return false;
        }
        String loaderName = classLoader.getClass().getName();
        return isClassLoaderNameMatches(loaderName);
    }

    boolean isClassLoaderNameMatches(String className) {
        return className.startsWith(LAUNCHED_URL_CLASS_LOADER_PREFIX)
                && (className.endsWith(LAUNCHED_URL_CLASS_LOADER_SUFFIX)
                        || className.endsWith(LAUNCHED_CLASS_LOADER_SUFFIX));
    }
}
