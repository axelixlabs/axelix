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

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.jspecify.annotations.Nullable;

import com.axelixlabs.axelix.master.domain.ecosystem.libraries.Library;

/**
 * The parsed dependency SBOM of a managed application: the application itself, every softwareProject resolved onto its
 * runtime classpath, and the chain each softwareProject was pulled in along.
 *
 * @author Mikhail Polivakha
 */
public class ParsedSbom {

    private final String rootCoordinates;
    private final Map<String, ResolvedSbomComponent> componentsByRef;
    private final ComponentsGraph graph;

    ParsedSbom(String rootCoordinates, List<ResolvedSbomComponent> components, ComponentsGraph graph) {
        this.rootCoordinates = rootCoordinates;
        this.componentsByRef =
                components.stream().collect(Collectors.toMap(ResolvedSbomComponent::bomRef, Function.identity()));
        this.graph = graph;
    }

    /**
     * @return the {@code groupId:artifactId:version} libraries of the analyzed application itself, or the raw name
     *         the build reported when the build declares no group or version
     */
    public String rootCoordinates() {
        return rootCoordinates;
    }

    public List<ResolvedSbomComponent> components() {
        return new ArrayList<>(componentsByRef.values());
    }

    /**
     * The chain of {@code groupId:artifactId:version} libraries leading from the root application to the given
     * softwareProject, the softwareProject itself being the last element and the root excluded. A chain of a single element is a
     * direct dependency.
     *
     * @param component the softwareProject the path leads to
     *
     * @return the resolution path of the softwareProject
     */
    public List<String> resolutionPathOf(ResolvedSbomComponent component) {
        return graph.pathTo(component.bomRef()).stream().map(this::gavOf).toList();
    }

    private String gavOf(String ref) {
        ResolvedSbomComponent component = componentsByRef.get(ref);
        return component != null ? component.gav() : ref;
    }

    /**
     * A single softwareProject the SBOM reports on the runtime classpath of a managed application.
     *
     * @param coordinates the version-free libraries the softwareProject is looked up by in the curated catalog
     * @param version     the version that was actually resolved
     * @param bomRef      the identifier the SBOM's dependency graph refers to this softwareProject by
     *
     * @author Mikhail Polivakha
     */
    public record ResolvedSbomComponent(Library coordinates, String version, String bomRef) {

        public String gav() {
            return coordinates + ":" + version;
        }
    }

    /**
     * The DAG of components. Contains backreferences to the parent so we can render the dependency resolution path on the UI.
     *
     * @author Mikhail Polivakha
     */
    static class ComponentsGraph {

        /**
         * Key - ref of the child in the BOM.
         * Value - ref of the parent of the given child in the BOM.
         */
        private final Map<String, @Nullable String> parents;

        /**
         * @param rootRef the reference of the root application, where every path starts
         * @param edges   the outgoing edges (i.e. from parent --> child) of every node, keyed by node reference
         */
        ComponentsGraph(String rootRef, Map<String, List<String>> edges) {
            this.parents = parentsOf(rootRef, edges);
        }

        private static Map<String, @Nullable String> parentsOf(String rootRef, Map<String, List<String>> edges) {
            Map<String, @Nullable String> parents = new HashMap<>();

            // root has no parent
            parents.put(rootRef, null);

            Queue<String> frontier = new ArrayDeque<>();
            frontier.add(rootRef);

            while (!frontier.isEmpty()) {
                String current = frontier.remove();

                for (String child : edges.getOrDefault(current, List.of())) {
                    if (!parents.containsKey(child)) {
                        parents.put(child, current);
                        frontier.add(child);
                    }
                }
            }

            return parents;
        }

        List<String> pathTo(String ref) {
            if (!parents.containsKey(ref)) {
                // dependency has no parent - it is the root dependency
                return List.of(ref);
            }

            List<String> path = new ArrayList<>();

            String current = ref;
            String parent = parents.get(current);

            while (parent != null && !parent.equals(current)) {
                path.add(current);
                current = parent;
                parent = parents.get(current);
            }

            Collections.reverse(path);

            return path.isEmpty() ? List.of(ref) : path;
        }
    }
}
