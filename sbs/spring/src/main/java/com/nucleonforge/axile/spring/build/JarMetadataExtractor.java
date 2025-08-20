package com.nucleonforge.axile.spring.build;

import java.util.jar.JarFile;

import com.nucleonforge.axile.common.domain.FlatJarClassPathEntry;

/**
 * Strategy interface for extracting metadata from a JAR file.
 *
 * @since 19.08.2025
 * @author Nikita Kirillov
 */
public interface JarMetadataExtractor {

    /**
     * Extracts metadata from the given JAR file and returns it as a {@link FlatJarClassPathEntry}.
     *
     * @param jarFile the JAR file to analyze
     * @return extracted metadata, or {@code null} if this extractor cannot handle the file
     */
    FlatJarClassPathEntry extract(JarFile jarFile);
}
