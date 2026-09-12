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
package com.axelixlabs.axelix.master.service.dependencies;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.dataformat.yaml.YAMLMapper;

import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

import com.axelixlabs.axelix.master.domain.dependencies.KnownLibrary;

/**
 * Reads every curated manifest off the classpath and flattens them into the entries a {@link LibraryCatalog} is built
 * from.
 * <p>
 * The pattern is a {@code classpath*:} one on purpose, so that a distribution can contribute additional manifests
 * from its own jar without this class knowing about it. Files are read in filename order so that a duplicate
 * reported by {@link DefaultLibraryCatalog} always names the same two entries, whatever order the classpath happens
 * to hand them over in.
 * <p>
 * Unknown properties are rejected rather than ignored. The manifests are data Axelix authors by hand, and a
 * misspelled key that is silently dropped would mean a curated fact quietly disappearing from the product.
 *
 * @author Mikhail Polivakha
 */
public class LibraryManifestLoader {

    static final String DEFAULT_LOCATION_PATTERN = "classpath*:axelix/dependencies/*.yaml";

    private static final Comparator<Resource> BY_FILENAME =
            Comparator.comparing(resource -> String.valueOf(resource.getFilename()));

    private final ResourcePatternResolver resourceResolver;
    private final ObjectMapper yamlMapper;
    private final String locationPattern;

    public LibraryManifestLoader() {
        this(new PathMatchingResourcePatternResolver(), DEFAULT_LOCATION_PATTERN);
    }

    /**
     * @param resourceResolver the resolver the manifests are looked up through
     * @param locationPattern  the Ant-style pattern the manifests are located by
     */
    public LibraryManifestLoader(ResourcePatternResolver resourceResolver, String locationPattern) {
        this.resourceResolver = resourceResolver;
        this.locationPattern = locationPattern;
        this.yamlMapper = YAMLMapper.builder()
                .enable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .build();
    }

    /**
     * Reads every manifest matching the configured pattern.
     *
     * @return the curated entries of all manifests, concatenated in filename order
     *
     * @throws LibraryCatalogException when a manifest cannot be located, read or parsed
     */
    public List<KnownLibrary> load() {
        Resource[] manifests;

        try {
            manifests = resourceResolver.getResources(locationPattern);
        } catch (IOException e) {
            throw new LibraryCatalogException(
                    "Failed to locate the library manifests at '%s'".formatted(locationPattern), e);
        }

        Arrays.sort(manifests, BY_FILENAME);

        List<KnownLibrary> libraries = new ArrayList<>();

        for (Resource manifest : manifests) {
            libraries.addAll(read(manifest));
        }

        return libraries;
    }

    private List<KnownLibrary> read(Resource manifest) {
        try (InputStream source = manifest.getInputStream()) {
            return yamlMapper.readValue(source, LibraryManifest.class).toLibraries();
        } catch (IOException | JacksonException | IllegalArgumentException e) {
            throw new LibraryCatalogException(
                    "Failed to read the library manifest %s".formatted(manifest.getDescription()), e);
        }
    }
}
