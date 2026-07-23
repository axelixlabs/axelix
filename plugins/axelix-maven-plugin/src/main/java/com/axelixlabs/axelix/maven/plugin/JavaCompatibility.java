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
package com.axelixlabs.axelix.maven.plugin;

import java.io.DataInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.stream.Stream;

import org.apache.maven.model.Plugin;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.jspecify.annotations.Nullable;

/**
 * Determines the Java release a maven project compiles to, mirroring the gradle plugin's {@code
 * JavaCompatibility}.
 *
 * @author Nikita Kirillov
 */
public final class JavaCompatibility {

    private static final String COMPILER_PLUGIN_KEY = "org.apache.maven.plugins:maven-compiler-plugin";

    /**
     * {@code class} file major version of Java 1.0.
     */
    private static final int MAJOR_VERSION_BASE = 44;

    private JavaCompatibility() {}

    /**
     * @return {@code true} if the project's {@code maven-compiler-plugin} targets at least
     * {@code minimumFeatureVersion}. Returns {@code false} if the target cannot be determined at all.
     */
    public static boolean compilesToAtLeast(MavenProject mavenProject, int minimumFeatureVersion) {
        Integer effective = effectiveJavaVersion(mavenProject);
        return effective != null && effective >= minimumFeatureVersion;
    }

    private static @Nullable Integer effectiveJavaVersion(MavenProject mavenProject) {
        Integer fromPlugin = readCompilerPluginVersion(mavenProject);
        if (fromPlugin != null) {
            return fromPlugin;
        }

        Integer fromProperties = readPropertyVersion(mavenProject);
        if (fromProperties != null) {
            return fromProperties;
        }

        // Neither an explicit <release>/<target> nor a maven.compiler.* property is set, so the
        // actual target depends on maven-compiler-plugin's own version-specific default (it isn't
        // exposed as static plugin metadata, only computed inside that plugin's own Java code at
        // execution time) - there's no reliable way to predict it from the outside. We deliberately
        // don't fall back to the JVM running this build either: that's the version of Maven itself,
        // unrelated to what the project's sources were actually compiled to. Instead, since this
        // mojo binds to PREPARE_PACKAGE, compile/test-compile have already produced .class files by
        // now, so we read the ground truth straight out of the compiled bytecode's major version.
        return readBytecodeVersion(mavenProject);
    }

    private static @Nullable Integer readCompilerPluginVersion(MavenProject mavenProject) {
        Plugin plugin = mavenProject.getBuild().getPluginsAsMap().get(COMPILER_PLUGIN_KEY);
        if (plugin == null || !(plugin.getConfiguration() instanceof Xpp3Dom)) {
            return null;
        }

        Xpp3Dom configuration = (Xpp3Dom) plugin.getConfiguration();
        Integer release = readChild(configuration, "release");
        return release != null ? release : readChild(configuration, "target");
    }

    private static @Nullable Integer readChild(Xpp3Dom configuration, String childName) {
        Xpp3Dom child = configuration.getChild(childName);
        return child == null ? null : parseVersion(child.getValue());
    }

    private static @Nullable Integer readPropertyVersion(MavenProject mavenProject) {
        Integer release = parseVersion(mavenProject.getProperties().getProperty("maven.compiler.release"));
        return release != null
                ? release
                : parseVersion(mavenProject.getProperties().getProperty("maven.compiler.target"));
    }

    private static @Nullable Integer parseVersion(@Nullable String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        String normalized = value.startsWith("1.") ? value.substring(2) : value;
        try {
            return Integer.parseInt(normalized.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static @Nullable Integer readBytecodeVersion(MavenProject mavenProject) {
        Integer fromMainClasses =
                readBytecodeVersion(Path.of(mavenProject.getBuild().getOutputDirectory()));
        return fromMainClasses != null
                ? fromMainClasses
                : readBytecodeVersion(Path.of(mavenProject.getBuild().getTestOutputDirectory()));
    }

    private static @Nullable Integer readBytecodeVersion(Path classesDirectory) {
        if (!Files.isDirectory(classesDirectory)) {
            return null;
        }

        try (Stream<Path> files = Files.walk(classesDirectory)) {
            Optional<Path> classFile =
                    files.filter(path -> path.toString().endsWith(".class")).findFirst();
            if (classFile.isEmpty()) {
                return null;
            }

            return readMajorVersion(classFile.get()) - MAJOR_VERSION_BASE;
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * Reads the {@code class} file's major version - bytes 6-7.
     */
    private static int readMajorVersion(Path classFile) throws IOException {
        try (DataInputStream in = new DataInputStream(Files.newInputStream(classFile))) {
            in.skipBytes(6);
            return in.readUnsignedShort();
        }
    }
}
