package com.nucleonforge.axile.spring.build;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.Properties;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Stream;

import com.nucleonforge.axile.common.domain.FlatJarClassPathEntry;
import com.nucleonforge.axile.spring.utils.StringUtils;

/**
 * Implementation of {@link JarMetadataExtractor} that extracts flat metadata
 * from Maven-based JAR files.
 * <p>
 * Produces {@link FlatJarClassPathEntry} without transitive dependency information.
 *
 * @since 19.08.2025
 * @author Nikita Kirillov
 */
public class FlatMavenMetadataExtractor implements JarMetadataExtractor {

    @Override
    public FlatJarClassPathEntry extract(JarFile jarFile) {
        FlatJarClassPathEntry fromProperties = tryExtractFromPomProperties(jarFile);
        if (fromProperties != null) {
            return fromProperties;
        }
        return tryExtractFromPomXml(jarFile);
    }

    private FlatJarClassPathEntry tryExtractFromPomProperties(JarFile jarFile) {
        Enumeration<JarEntry> entries = jarFile.entries();

        while (entries.hasMoreElements()) {
            JarEntry entry = entries.nextElement();
            String entryName = entry.getName();

            if (entryName.startsWith("META-INF/maven/") && entryName.endsWith("/pom.properties")) {
                try (InputStream is = jarFile.getInputStream(entry)) {
                    Properties props = new Properties();
                    props.load(is);

                    String groupId = props.getProperty("groupId");
                    String artifactId = props.getProperty("artifactId");
                    String version = props.getProperty("version");

                    if (groupId != null && artifactId != null && version != null) {
                        return new FlatJarClassPathEntry(
                                StringUtils.defaultIfBlank(groupId, "unknown-group"),
                                StringUtils.defaultIfBlank(artifactId, "unknown-artifact"),
                                StringUtils.defaultIfBlank(version, "unknown-version"));
                    }
                } catch (IOException ignored) {
                }
            }
        }
        return null;
    }

    private FlatJarClassPathEntry tryExtractFromPomXml(JarFile jarFile) {
        Path jarPath = Paths.get(new File(jarFile.getName()).getAbsolutePath());
        Path pomPath = findPomPath(jarPath);

        if (pomPath == null) {
            return null;
        }

        try {
            String pomContent = Files.readString(pomPath, StandardCharsets.UTF_8);

            String groupId = extractXmlValue(pomContent, "groupId");
            String artifactId = extractXmlValue(pomContent, "artifactId");
            String version = extractXmlValue(pomContent, "version");

            return new FlatJarClassPathEntry(
                    StringUtils.defaultIfBlank(groupId, "unknown-group"),
                    StringUtils.defaultIfBlank(artifactId, "unknown-artifact"),
                    StringUtils.defaultIfBlank(version, "unknown-version"));

        } catch (IOException ignored) {
            return null;
        }
    }

    private Path findPomPath(Path jarPath) {
        // Check in the same directory as the JAR
        try (Stream<Path> files = Files.list(jarPath.getParent())) {
            Path pomPath =
                    files.filter(p -> p.toString().endsWith(".pom")).findFirst().orElse(null);
            if (pomPath != null) {
                return pomPath;
            }
        } catch (IOException ignore) {
        }

        // If not found, go one level up and search recursively in all subdirectories
        Path parent = jarPath.getParent();
        if (parent != null && parent.getParent() != null) {
            try (Stream<Path> files = Files.walk(parent.getParent())) {
                return files.filter(p -> p.toString().endsWith(".pom"))
                        .findFirst()
                        .orElse(null);
            } catch (IOException ignore) {
            }
        }
        return null;
    }

    private String extractXmlValue(String xml, String tagName) {
        String startTag = "<" + tagName + ">";
        String endTag = "</" + tagName + ">";
        int startIndex = xml.indexOf(startTag);
        if (startIndex == -1) return null;

        startIndex += startTag.length();
        int endIndex = xml.indexOf(endTag, startIndex);
        if (endIndex == -1) return null;

        return xml.substring(startIndex, endIndex).trim();
    }
}
