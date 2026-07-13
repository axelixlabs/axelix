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
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.jar.Attributes;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.CleanupMode;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import com.axelixlabs.axelix.sbs.spring.core.testutils.NoOpLogger;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit test for {@link DefaultJarManifestInspector}.
 *
 * @author Ilya Naumov
 */
class DefaultJarManifestInspectorTest {

    private final DefaultJarManifestInspector inspector = new DefaultJarManifestInspector(new NoOpLogger());

    @TempDir(cleanup = CleanupMode.ALWAYS)
    private File tempDir;

    @Test // GH-1219
    void returnsTrue_whenClassPathAttributeIsNonEmpty() throws Exception {
        // given.
        File jarFile = createJarWithClassPathAttribute("lib/spring-boot.jar");

        // when.
        boolean result = inspector.hasNonEmptyClassPath(jarFile.toURI().toURL());

        // then.
        assertThat(result).isTrue();
    }

    @ParameterizedTest // GH-1219
    @ValueSource(strings = {"", " "})
    void returnsFalse_whenClassPathValueIsInvalid(String classPathValue) throws Exception {
        // given.
        File jarFile = createJarWithClassPathAttribute(classPathValue);

        // when.
        boolean result = inspector.hasNonEmptyClassPath(jarFile.toURI().toURL());

        // then.
        assertThat(result).isFalse();
    }

    @Test // GH-1219
    void returnsFalse_whenClassPathAttributeIsMissing() throws Exception {
        // given.
        File jarFile = createJarWithoutClassPath();

        // when.
        boolean result = inspector.hasNonEmptyClassPath(jarFile.toURI().toURL());

        // then.
        assertThat(result).isFalse();
    }

    @Test // GH-1219
    void returnsFalse_whenManifestIsNull() throws Exception {
        // given.
        File jarFile = createJarWithoutManifest();

        // when.
        boolean result = inspector.hasNonEmptyClassPath(jarFile.toURI().toURL());

        // then.
        assertThat(result).isFalse();
    }

    @Test // GH-1219
    void returnsFalse_whenProtocolIsNotFile() throws Exception {
        // given.
        URL jarUrl = new URL("jar:file:/application/app.jar!/BOOT-INF/classes/");

        // when.
        boolean result = inspector.hasNonEmptyClassPath(jarUrl);

        // then.
        assertThat(result).isFalse();
    }

    private File createJarWithClassPathAttribute(String classPathValue) throws IOException {
        File jarFile = new File(tempDir, "valid.jar");
        Manifest manifest = new Manifest();
        manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
        manifest.getMainAttributes().put(Attributes.Name.CLASS_PATH, classPathValue);

        // Creating and closing the JarOutputStream is enough to write the JAR.
        try (FileOutputStream fos = new FileOutputStream(jarFile);
                JarOutputStream jos = new JarOutputStream(fos, manifest)) {}
        return jarFile;
    }

    private File createJarWithoutClassPath() throws IOException {
        File jarFile = new File(tempDir, "no-classpath.jar");
        Manifest manifest = new Manifest();
        manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");

        // Creating and closing the JarOutputStream is enough to write the JAR.
        try (FileOutputStream fos = new FileOutputStream(jarFile);
                JarOutputStream jos = new JarOutputStream(fos, manifest)) {}
        return jarFile;
    }

    private File createJarWithoutManifest() throws IOException {
        File jarFile = new File(tempDir, "no-manifest.jar");

        // Creating and closing the JarOutputStream is enough to write the JAR.
        try (FileOutputStream fos = new FileOutputStream(jarFile);
                JarOutputStream jos = new JarOutputStream(fos)) {}
        return jarFile;
    }
}
