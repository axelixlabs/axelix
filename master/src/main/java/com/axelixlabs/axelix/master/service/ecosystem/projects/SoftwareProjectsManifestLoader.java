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
package com.axelixlabs.axelix.master.service.ecosystem.projects;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.jspecify.annotations.Nullable;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.dataformat.yaml.YAMLMapper;

import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;

import com.axelixlabs.axelix.master.domain.ecosystem.libraries.Library;
import com.axelixlabs.axelix.master.domain.ecosystem.projects.Ecosystem;
import com.axelixlabs.axelix.master.domain.ecosystem.projects.ProjectReference;
import com.axelixlabs.axelix.master.domain.ecosystem.projects.SoftwareProject;
import com.axelixlabs.axelix.master.domain.ecosystem.projects.SoftwareProjectId;
import com.axelixlabs.axelix.master.domain.ecosystem.projects.Succession;
import com.axelixlabs.axelix.master.domain.ecosystem.projects.SupportStatus;

/**
 * Reads every curated manifest off the classpath and flattens them into the entries a {@link SoftwareProjectsCatalog} is built
 * from.
 * <p>
 * The pattern is a {@code classpath*:} one on purpose, so that a distribution can contribute additional manifests
 * from its own jar without this class knowing about it. Files are read in filename order so that a duplicate
 * reported by {@link DefaultSoftwareProjectsCatalog} always names the same two entries, whatever order the classpath happens
 * to hand them over in.
 * <p>
 * Unknown properties are rejected rather than ignored. The manifests are data Axelix authors by hand, and a
 * misspelled key that is silently dropped would mean a curated fact quietly disappearing from the product.
 *
 * @author Mikhail Polivakha
 */
public class SoftwareProjectsManifestLoader {

    public static final String DEFAULT_LOCATION_PATTERN = "classpath*:axelix/software-projects/*.yaml";

    private static final Comparator<Resource> BY_FILENAME =
            Comparator.comparing(resource -> String.valueOf(resource.getFilename()));

    private final ResourcePatternResolver resourceResolver;
    private final ObjectMapper yamlMapper;
    private final String locationPattern;

    /**
     * @param resourceResolver the resolver the manifests are looked up through
     * @param locationPattern  the Ant-style pattern the manifests are located by
     */
    public SoftwareProjectsManifestLoader(ResourcePatternResolver resourceResolver, String locationPattern) {
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
     * @throws SoftwareProjectsCatalogException when a manifest cannot be located, read or parsed
     */
    public List<SoftwareProject> load() {
        Resource[] manifests;

        try {
            manifests = resourceResolver.getResources(locationPattern);
        } catch (IOException e) {
            throw new SoftwareProjectsCatalogException(
                    "Failed to locate the software project manifests at '%s'".formatted(locationPattern), e);
        }

        Arrays.sort(manifests, BY_FILENAME);

        List<SoftwareProject> libraries = new ArrayList<>();

        for (Resource manifest : manifests) {
            libraries.addAll(read(manifest));
        }

        return libraries;
    }

    private List<SoftwareProject> read(Resource manifest) {
        try (InputStream source = manifest.getInputStream()) {
            return yamlMapper.readValue(source, SoftwareProjectManifest.class).toSoftwareProjects();
        } catch (IOException | JacksonException | IllegalArgumentException e) {
            throw new SoftwareProjectsCatalogException(
                    "Failed to read the software projects manifest %s".formatted(manifest.getDescription()), e);
        }
    }

    /**
     * The on-disk shape of one curated manifest file. Manifest files are broken down into the
     *
     * @param ecosystem the area every software project in this file belongs to
     * @param projects the curated entries
     *
     * @author Mikhail Polivakha
     */
    record SoftwareProjectManifest(Ecosystem ecosystem, List<Entry> projects) {

        List<SoftwareProject> toSoftwareProjects() {
            return projects.stream()
                    .map(entry -> entry.toSoftwareProject(ecosystem))
                    .toList();
        }

        /**
         * @param id          the stable identifier, in lower kebab-case
         * @param name        the name the project is known by
         * @param status      what the authors are still doing with the project
         * @param summary     what happened to the project, in prose
         * @param coordinates every artifact of the project, in the {@code groupId:artifactId} notation
         * @param succession  where to go instead, omitted for an active project
         * @param reference   the upstream page backing the status
         */
        record Entry(
                String id,
                String name,
                SupportStatus status,
                String summary,
                List<String> coordinates,
                @Nullable SuccessionEntry succession,
                ReferenceEntry reference) {

            SoftwareProject toSoftwareProject(Ecosystem ecosystem) {
                return new SoftwareProject(
                        SoftwareProjectId.of(id),
                        name,
                        ecosystem,
                        status,
                        summary,
                        coordinates.stream().map(Library::parse).collect(Collectors.toUnmodifiableSet()),
                        succession == null ? null : new Succession(succession.kind(), succession.value()),
                        ProjectReference.of(reference.label(), reference.url()));
            }
        }

        record SuccessionEntry(Succession.Kind kind, String value) {}

        record ReferenceEntry(String label, String url) {}
    }
}
