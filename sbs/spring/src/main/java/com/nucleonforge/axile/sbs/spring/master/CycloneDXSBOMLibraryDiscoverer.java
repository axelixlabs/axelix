package com.nucleonforge.axile.sbs.spring.master;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

import com.jayway.jsonpath.JsonPath;
import net.minidev.json.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.core.io.ClassPathResource;
import org.springframework.util.CollectionUtils;

// TODO I think it would be a great idea to create a thread-safe caching decorator over this class
/**
 * {@link LibraryDiscoverer} that discovers version of various components via inspecting the
 * CycloneDX Json SBOM.
 *
 * @author Mikhail Polivakha
 */
public class CycloneDXSBOMLibraryDiscoverer implements LibraryDiscoverer {

    /**
     * Template for searching the version of the given library inside the CycloneDX SBOM Json.
     */
    private static final String JSON_PATH_SEARCH_QUERY_TEMPLATE;

    private static final String DEFAULT_SBOM_JSON_LOCATION = "META-INF/sbom/application.cdx.json";

    private static final Logger log = LoggerFactory.getLogger(CycloneDXSBOMLibraryDiscoverer.class);

    private final ClassPathResource cycloneDXJsonSbom;

    static {
        JSON_PATH_SEARCH_QUERY_TEMPLATE = "$.components[?(@.name == '%s' && @.group == '%s')]";
    }

    public CycloneDXSBOMLibraryDiscoverer() {
        this(new ClassPathResource(DEFAULT_SBOM_JSON_LOCATION));
    }

    public CycloneDXSBOMLibraryDiscoverer(ClassPathResource cycloneDXJsonSbom) {
        this.cycloneDXJsonSbom = cycloneDXJsonSbom;

        if (!cycloneDXJsonSbom.exists()) {
            throw new IllegalArgumentException("There is no CycloneDX SBOM found in classpath by location %s"
                    .formatted(DEFAULT_SBOM_JSON_LOCATION));
        }
    }

    @Override
    public Optional<String> getLibraryVersion(String artifactId, String groupId) {
        String searchQuery = JSON_PATH_SEARCH_QUERY_TEMPLATE.formatted(artifactId, groupId);
        try {
            JSONArray result = JsonPath.read(cycloneDXJsonSbom.getInputStream(), searchQuery);

            if (result.isEmpty()) {
                logEmptyResult(artifactId, groupId);
                return Optional.empty();
            }

            if (result.size() > 1) {
                logMultipleEntriesFound(artifactId, groupId);
            }

            @SuppressWarnings("unchecked")
            var entry = (Map<String, Object>) result.get(0);

            String version;

            if (CollectionUtils.isEmpty(entry) || (version = (String) entry.get("version")) == null) {
                logEmptyResult(artifactId, groupId);
                return Optional.empty();
            }

            return Optional.of(version);
        } catch (IOException e) {
            log.warn(
                    "Unexpected exception while reading the information for library [{}:{}] from SBOM",
                    groupId,
                    artifactId,
                    e);
            return Optional.empty();
        }
    }

    private static void logMultipleEntriesFound(String artifactId, String groupId) {
        log.warn(
                "Found multiple artifacts identified by groupId: {} and artifactID: {} inside CycloneDX Json SBOM. That is typically not an expected outcome",
                groupId,
                artifactId);
    }

    private static void logEmptyResult(String artifactId, String groupId) {
        log.trace(
                "Artifact identified by groupId: {} and artifactID: {} was not found in CycloneDX Json SBOM",
                groupId,
                artifactId);
    }
}
