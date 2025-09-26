package com.nucleonforge.axile.spring.build;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.jar.JarFile;

import com.nucleonforge.axile.common.domain.ClassPath;
import com.nucleonforge.axile.common.domain.ClassPathEntry;
import com.nucleonforge.axile.common.domain.FlatJarClassPathEntry;

/**
 * A {@link ClassPathDiscoverer} implementation that produces a flat representation
 * of the application's classpath.
 *
 * @see FlatJarClassPathEntry
 * @see JarMetadataExtractor
 * @since 18.08.2025
 * @author Nikita Kirillov
 */
public class FlatClassPathDiscoverer implements ClassPathDiscoverer {

    private final List<JarMetadataExtractor> extractors;

    public FlatClassPathDiscoverer(List<JarMetadataExtractor> extractors) {
        this.extractors = extractors;
    }

    @Override
    public ClassPath discover() {
        String classPathProperty = System.getProperty("java.class.path");

        if (classPathProperty != null && !classPathProperty.isEmpty()) {
            return new ClassPath(discoverFromClassPath(classPathProperty));
        }

        return new ClassPath(Collections.emptySet());
    }

    private Set<ClassPathEntry> discoverFromClassPath(String classPathProperty) {
        Set<ClassPathEntry> entries = new HashSet<>();
        String[] paths = classPathProperty.split(File.pathSeparator);

        for (String path : paths) {
            Path jarPath = Paths.get(path);
            if (Files.exists(jarPath) && path.endsWith(".jar")) {
                try (JarFile jarFile = new JarFile(jarPath.toFile())) {
                    FlatJarClassPathEntry entry = extractMetadata(jarFile);
                    if (entry != null) {
                        entries.add(entry);
                    }
                } catch (IOException ignored) {
                }
            }
        }

        return entries;
    }

    private FlatJarClassPathEntry extractMetadata(JarFile jarFile) {
        for (JarMetadataExtractor extractor : extractors) {
            FlatJarClassPathEntry entry = extractor.extract(jarFile);
            if (entry != null) {
                return entry;
            }
        }
        return null;
    }
}
