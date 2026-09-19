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
package com.axelixlabs.axelix.master.service.ecosystem.sbom;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.jspecify.annotations.Nullable;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import com.axelixlabs.axelix.master.domain.ecosystem.libraries.Library;
import com.axelixlabs.axelix.master.service.ecosystem.sbom.ParsedSbom.ComponentsGraph;
import com.axelixlabs.axelix.master.service.ecosystem.sbom.ParsedSbom.ResolvedSbomComponent;
import com.axelixlabs.axelix.master.service.ecosystem.sbom.SbomParser.CycloneDxDocument.Component;
import com.axelixlabs.axelix.master.service.ecosystem.sbom.SbomParser.CycloneDxDocument.Dependency;

/**
 * Parses the CycloneDX SBOM a managed application serves into a {@link ParsedSbom}.
 * <p>
 * Only libraries carrying complete libraries (group, name and version) are kept. A component without them can
 * neither be looked up in the curated catalog nor rendered as a dependency row, and the Axelix build plugins only
 * emit such components for the root application itself.
 *
 * @author Mikhail Polivakha
 */
public class SbomParser {

    private final ObjectMapper jsonMapper;

    public SbomParser() {
        this.jsonMapper = JsonMapper.builder()
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .build();
    }

    /**
     * @param binarySbom the raw CycloneDX JSON document, as served by the managed application
     *
     * @return the parsed SBOM
     *
     * @throws SbomParsingException when the document is not valid CycloneDX JSON or carries no root application
     */
    public ParsedSbom parse(byte[] binarySbom) {
        CycloneDxDocument sbom;

        try {
            sbom = jsonMapper.readValue(binarySbom, CycloneDxDocument.class);
        } catch (JacksonException e) {
            throw new SbomParsingException("Failed to parse the dependency SBOM as CycloneDX JSON", e);
        }

        // root represents the end user's application itself
        Component root = sbom.metadata() != null ? sbom.metadata().component() : null;

        if (root == null) {
            throw new SbomParsingException("The dependency SBOM carries no root application in metadata.component");
        }

        return new ParsedSbom(coordinatesOf(root), componentsOf(sbom), graphOf(rootRefOf(root), sbom));
    }

    private static String rootRefOf(Component root) {
        return root.bomRef() != null ? root.bomRef() : root.name();
    }

    /**
     * The root application falls back to its bare name when the build declares no group or version, mirroring the
     * fallback of the build plugins.
     */
    private static String coordinatesOf(Component component) {
        if (component.group() == null || component.version() == null) {
            return component.name();
        }
        return component.group() + ":" + component.name() + ":" + component.version();
    }

    private static List<ResolvedSbomComponent> componentsOf(CycloneDxDocument document) {
        List<ResolvedSbomComponent> components = new ArrayList<>();

        for (Component component : orEmpty(document.components())) {
            if (component.bomRef() == null || component.group() == null || component.version() == null) {
                continue;
            }
            components.add(new ResolvedSbomComponent(
                    Library.of(component.group(), component.name()), component.version(), component.bomRef()));
        }

        return components;
    }

    private static ComponentsGraph graphOf(String rootRef, CycloneDxDocument document) {
        Map<String, List<String>> edges = new HashMap<>();

        for (Dependency dependency : orEmpty(document.dependencies())) {
            edges.put(dependency.ref(), orEmpty(dependency.dependsOn()));
        }

        return new ComponentsGraph(rootRef, edges);
    }

    private static <T> List<T> orEmpty(@Nullable List<T> list) {
        return list != null ? list : List.of();
    }

    /**
     * The shape of the CycloneDX SBOM the Axelix build plugins generate, reduced to the few fields Axelix Master actually needs.
     *
     * @author Mikhail Polivakha
     */
    record CycloneDxDocument(
            @Nullable Metadata metadata,
            @Nullable List<Component> components,
            @Nullable List<Dependency> dependencies) {

        /**
         * @param component the root application the SBOM was generated for
         */
        record Metadata(@Nullable Component component) {}

        /**
         * @param bomRef  the identifier the {@link Dependency dependency graph} refers to this component by
         * @param group   the group id; the build plugins always fill it in for libraries, but may omit it for the root
         *                application when the build declares no group
         * @param name    the artifact id
         * @param version the resolved version; may be absent for the root application
         */
        record Component(
                @JsonProperty("bom-ref") @Nullable String bomRef,
                @Nullable String group,
                String name,
                @Nullable String version) {}

        /**
         * One node of the dependency graph together with its outgoing edges.
         *
         * @param ref       the {@link Component#bomRef()} of the depending component
         * @param dependsOn the {@link Component#bomRef()}s of its direct dependencies
         */
        record Dependency(String ref, @Nullable List<String> dependsOn) {}
    }
}
