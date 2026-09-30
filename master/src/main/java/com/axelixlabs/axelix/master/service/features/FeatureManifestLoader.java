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
package com.axelixlabs.axelix.master.service.features;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.dataformat.yaml.YAMLMapper;

import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import com.axelixlabs.axelix.common.utils.SemanticVersion;
import com.axelixlabs.axelix.master.domain.features.Feature;

/**
 * Reads the curated feature manifest off the classpath into the {@link Feature Features} a {@link StarterFeaturesCatalog} is
 * built from.
 * <p>
 * The manifest is authored by release: each entry names the release that introduced a set of features, which reads
 * naturally as the product grows and keeps a feature's introduction version in a single place. It is flattened here
 * into one {@link Feature} per id, tagged with the release it became available in.
 * <p>
 * Unknown properties are rejected rather than ignored: the manifest is data Axelix authors by hand, and a misspelled
 * key that is silently dropped would mean a curated fact quietly disappearing from the product.
 *
 * @author Mikhail Polivakha
 */
public class FeatureManifestLoader {

    public static final String DEFAULT_LOCATION = "classpath:axelix/features/features.yaml";

    private final ResourceLoader resourceLoader;
    private final ObjectMapper yamlMapper;
    private final String location;

    /**
     * @param resourceLoader the loader the manifest is looked up through
     * @param location       the location the manifest is read from
     */
    public FeatureManifestLoader(ResourceLoader resourceLoader, String location) {
        this.resourceLoader = resourceLoader;
        this.location = location;
        this.yamlMapper = YAMLMapper.builder()
                .enable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .build();
    }

    /**
     * Reads the manifest at the configured location.
     *
     * @return the curated features
     *
     * @throws FeatureCatalogException when the manifest cannot be located, read or parsed
     */
    public List<Feature> load() {
        Resource manifest = resourceLoader.getResource(location);

        try (InputStream source = manifest.getInputStream()) {
            return yamlMapper.readValue(source, FeatureManifest.class).toFeatures();
        } catch (IOException | JacksonException | IllegalArgumentException e) {
            throw new FeatureCatalogException(
                    "Failed to read the feature manifest %s".formatted(manifest.getDescription()), e);
        }
    }

    /**
     * The on-disk shape of the curated feature manifest: the features grouped by the release that introduced them.
     *
     * @param releases the curated releases
     */
    record FeatureManifest(List<Release> releases) {

        List<Feature> toFeatures() {
            return releases.stream()
                    .flatMap(release -> release.toFeatures().stream())
                    .toList();
        }

        /**
         * @param version  the Axelix lockstep release version, e.g. {@code 1.2.0}.
         * @param features the ids of the features introduced in this release
         */
        record Release(String version, List<String> features) {

            List<Feature> toFeatures() {
                SemanticVersion since = SemanticVersion.parse(version);
                return features.stream().map(id -> new Feature(id, since)).toList();
            }
        }
    }
}
