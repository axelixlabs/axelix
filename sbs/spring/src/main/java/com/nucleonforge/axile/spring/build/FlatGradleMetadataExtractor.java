package com.nucleonforge.axile.spring.build;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import com.nucleonforge.axile.common.domain.FlatJarClassPathEntry;
import com.nucleonforge.axile.spring.utils.StringUtils;

/**
 * Implementation of {@link JarMetadataExtractor} that extracts flat metadata
 * from a Gradle-generated JAR manifest ({@code META-INF/MANIFEST.MF}).
 * <p>
 * Produces {@link FlatJarClassPathEntry} without transitive dependency information.
 *
 * @since 19.08.2025
 * @author Nikita Kirillov
 */
public class FlatGradleMetadataExtractor implements JarMetadataExtractor {

    @Override
    public FlatJarClassPathEntry extract(JarFile jarFile) {
        try {
            JarEntry manifestEntry = jarFile.getJarEntry("META-INF/MANIFEST.MF");
            if (manifestEntry == null) return null;

            try (InputStream is = jarFile.getInputStream(manifestEntry)) {
                Manifest manifest = new Manifest(is);
                Attributes attrs = manifest.getMainAttributes();

                FlatJarClassPathEntry entry;

                entry = extractFromImplementation(attrs);
                if (entry != null) return entry;

                entry = extractFromBundle(attrs);
                if (entry != null) return entry;

                entry = extractFromAutomaticModule(attrs);
                if (entry != null) return entry;
            }
        } catch (IOException ignored) {
        }
        return null;
    }

    private FlatJarClassPathEntry extractFromImplementation(Attributes attrs) {
        String title = attrs.getValue("Implementation-Title");
        String version = attrs.getValue("Implementation-Version");
        String vendor = attrs.getValue("Implementation-Vendor");

        if (title != null || (vendor != null && version != null)) {
            return new FlatJarClassPathEntry(
                    StringUtils.defaultIfBlank(vendor, "unknown-group"),
                    StringUtils.defaultIfBlank(title, "unknown-artifact"),
                    StringUtils.defaultIfBlank(version, "unknown-version"));
        }
        return null;
    }

    private FlatJarClassPathEntry extractFromBundle(Attributes attrs) {
        String name = attrs.getValue("Bundle-SymbolicName");
        String version = attrs.getValue("Bundle-Version");

        if (name != null && version != null) {
            return splitNameAndBuildEntry(name, version);
        }
        return null;
    }

    private FlatJarClassPathEntry extractFromAutomaticModule(Attributes attrs) {
        String moduleName = attrs.getValue("Automatic-Module-Name");
        if (moduleName != null) {
            String version = StringUtils.defaultIfBlank(attrs.getValue("Implementation-Version"), "unknown-version");
            return splitNameAndBuildEntry(moduleName, version);
        }
        return null;
    }

    private FlatJarClassPathEntry splitNameAndBuildEntry(String fullName, String version) {
        String groupId = "unknown-group";
        String artifactId = fullName;

        if (fullName.contains(".")) {
            String[] parts = fullName.split("\\.");
            if (parts.length > 1) {
                groupId = String.join(".", Arrays.copyOf(parts, parts.length - 1));
                artifactId = parts[parts.length - 1];
            }
        }
        return new FlatJarClassPathEntry(groupId, artifactId, version);
    }
}
