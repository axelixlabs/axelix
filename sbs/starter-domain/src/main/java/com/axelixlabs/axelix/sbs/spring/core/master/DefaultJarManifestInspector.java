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

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import org.jspecify.annotations.Nullable;

import com.axelixlabs.axelix.sbs.spring.core.log.Logger;

/**
 * Default implementation of {@link JarManifestInspector}.
 *
 * @author Ilya Naumov
 */
public class DefaultJarManifestInspector implements JarManifestInspector {
    private final Logger logger;

    public DefaultJarManifestInspector(Logger logger) {
        this.logger = logger;
    }

    /**
     * Checks whether the JAR at the given location has a non-empty {@code Class-Path}
     * attribute in its MANIFEST.MF. Only {@code file} protocol is supported.
     * Returns {@code false} for non-file URLs, non-JAR files, or if the manifest
     * cannot be read.
     */
    @Override
    public boolean hasNonEmptyClassPath(URL jarLocation) {
        String protocol = jarLocation.getProtocol();
        if (!"file".equals(protocol)) {
            return false;
        }

        File file;
        try {
            file = new File(jarLocation.toURI());
        } catch (URISyntaxException e) {
            logger.warn("Invalid JAR location URI: {}", jarLocation, e);
            return false;
        }

        if (!file.getName().toLowerCase().endsWith(".jar")) {
            return false;
        }

        String classPathValue = readManifestClassPath(file);
        return classPathValue != null && !classPathValue.isBlank();
    }

    private @Nullable String readManifestClassPath(File file) {
        try (JarFile jarFile = new JarFile(file)) {
            Manifest manifest = jarFile.getManifest();
            if (manifest == null) {
                return null;
            }
            Attributes attributes = manifest.getMainAttributes();
            if (attributes == null) {
                return null;
            }
            return attributes.getValue(Attributes.Name.CLASS_PATH);
        } catch (IOException e) {
            logger.warn("Failed to read manifest from {}", file.getPath(), e);
            return null;
        }
    }
}
