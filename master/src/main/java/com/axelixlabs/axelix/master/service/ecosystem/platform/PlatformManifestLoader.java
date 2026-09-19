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
package com.axelixlabs.axelix.master.service.ecosystem.platform;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import org.jspecify.annotations.Nullable;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.dataformat.yaml.YAMLMapper;

import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

import com.axelixlabs.axelix.master.domain.ecosystem.platform.Platform;
import com.axelixlabs.axelix.master.domain.ecosystem.platform.PlatformName;
import com.axelixlabs.axelix.master.domain.ecosystem.platform.PlatformReleaseLine;

/**
 * Abstraction responsible to load the platforms from the manifest file.
 *
 * @author Mikhail Polivakha
 */
public class PlatformManifestLoader {

    static final String DEFAULT_LOCATION_PATTERN = "classpath*:axelix/platforms/*.yaml";

    private static final Comparator<Resource> BY_FILENAME =
            Comparator.comparing(resource -> String.valueOf(resource.getFilename()));

    private final ResourcePatternResolver resourceResolver;
    private final ObjectMapper yamlMapper;
    private final String locationPattern;

    public PlatformManifestLoader() {
        this(new PathMatchingResourcePatternResolver(), DEFAULT_LOCATION_PATTERN);
    }

    /**
     * @param resourceResolver the resolver the manifests are looked up through
     * @param locationPattern  the Ant-style pattern the manifests are located by
     */
    public PlatformManifestLoader(ResourcePatternResolver resourceResolver, String locationPattern) {
        this.resourceResolver = resourceResolver;
        this.locationPattern = locationPattern;
        this.yamlMapper = YAMLMapper.builder()
                .enable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .build();
    }

    /**
     * Reads every manifest matching the configured pattern.
     *
     * @return the curated policies of all manifests, in filename order
     *
     * @throws PlatformCatalogException when a manifest cannot be located, read or parsed
     */
    public List<Platform> load() {
        Resource[] manifests;

        try {
            manifests = resourceResolver.getResources(locationPattern);
        } catch (IOException e) {
            throw new PlatformCatalogException(
                    "Failed to locate the platform manifests at '%s'".formatted(locationPattern), e);
        }

        Arrays.sort(manifests, BY_FILENAME);

        List<Platform> policies = new ArrayList<>();

        for (Resource manifest : manifests) {
            policies.add(read(manifest));
        }

        return policies;
    }

    private Platform read(Resource manifest) {
        try (InputStream source = manifest.getInputStream()) {
            return yamlMapper.readValue(source, PlatformManifest.class).toPolicy();
        } catch (IOException | JacksonException | IllegalArgumentException e) {
            throw new PlatformCatalogException(
                    "Failed to read the platform manifest %s".formatted(manifest.getDescription()), e);
        }
    }

    /**
     * The on-disk shape of one curated platform manifest file.
     *
     * @param platform the name of the platform the file describes, e.g. {@code Spring Boot}.
     * @param lines    release lines of the platform and its maintenance windows.
     *
     * @author Mikhail Polivakha
     */
    record PlatformManifest(String platform, List<LineEntry> lines) {

        Platform toPolicy() {
            return new Platform(
                    PlatformName.valueOfElseThrow(platform),
                    lines.stream().map(LineEntry::toLine).toList());
        }

        /**
         * @param line                    the release line in the {@code major.minor.x} notation
         * @param releasedAt              the date the line was first released
         * @param ossSupportEndsAt        the date OSS maintenance of the line ended, or ends
         * @param commercialSupportEndsAt the date commercial support ends, omitted when the upstream offers none
         */
        record LineEntry(
                String line,
                LocalDate releasedAt,
                LocalDate ossSupportEndsAt,
                @Nullable LocalDate commercialSupportEndsAt) {

            PlatformReleaseLine toLine() {
                return new PlatformReleaseLine(line, releasedAt, ossSupportEndsAt, commercialSupportEndsAt);
            }
        }
    }
}
