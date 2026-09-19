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
package com.axelixlabs.axelix.sbs.spring.core.sbom;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.Optional;

/**
 * Reads the CycloneDX dependency SBOM that the Axelix build plugins package into the application
 * archive at {@value #DEFAULT_RESOURCE}. The resource is absent when the application was built
 * without an Axelix build plugin (which should not happen, since we require the presence of the build plugin),
 * in which case {@link #read()} returns an empty {@link Optional}.
 *
 * @author Mikhail Polivakha
 */
public class ClasspathDependencySbom {

    /**
     * Where the Axelix build plugins place the SBOM inside the archive. Must stay in sync with the
     * {@code SBOM_RESOURCE_PATH} constant of the Gradle plugin's {@code DependencySbomGenerator}.
     */
    public static final String DEFAULT_RESOURCE = "META-INF/axelix/dependencies.cdx.json";

    private final String resourceLocation;

    public ClasspathDependencySbom() {
        this(DEFAULT_RESOURCE);
    }

    /**
     * @param resourceLocation the classpath location of the SBOM. Exists so tests can exercise both
     *                         the present and the absent case on a single test classpath.
     */
    public ClasspathDependencySbom(String resourceLocation) {
        this.resourceLocation = resourceLocation;
    }

    public Optional<byte[]> read() {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        if (classLoader == null) {
            classLoader = ClasspathDependencySbom.class.getClassLoader();
        }
        try (InputStream sbom = classLoader.getResourceAsStream(resourceLocation)) {
            if (sbom == null) {
                return Optional.empty();
            }
            return Optional.of(sbom.readAllBytes());
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to read the Axelix dependency SBOM from " + resourceLocation, e);
        }
    }
}
