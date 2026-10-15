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
package com.axelixlabs.axelix.master.utils;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Builds minimal CycloneDX SBOMs for tests, shaped like the ones the Axelix build plugins generate.
 *
 * @author Nikita Kirillov
 */
public final class CycloneDxDocuments {

    private CycloneDxDocuments() {}

    /**
     * @param rootGav      the {@code groupId:artifactId:version} of the application itself
     * @param libraryGavs  the {@code groupId:artifactId:version} of the libraries, all direct dependencies of the root
     *
     * @return the SBOM as JSON
     */
    public static String of(String rootGav, String... libraryGavs) {
        String components =
                Arrays.stream(libraryGavs).map(CycloneDxDocuments::component).collect(Collectors.joining(","));
        String directDependencies =
                Arrays.stream(libraryGavs).map(gav -> "\"" + ref(gav) + "\"").collect(Collectors.joining(","));

        return """
                {
                  "bomFormat": "CycloneDX",
                  "specVersion": "1.6",
                  "version": 1,
                  "metadata": { "component": %s },
                  "components": [%s],
                  "dependencies": [ { "ref": "%s", "dependsOn": [%s] } ]
                }
                """.formatted(component(rootGav), components, ref(rootGav), directDependencies);
    }

    private static String component(String gav) {
        String[] coordinates = gav.split(":");
        return """
                { "bom-ref": "%s", "group": "%s", "name": "%s", "version": "%s" }""".formatted(ref(gav), coordinates[0], coordinates[1], coordinates[2]);
    }

    private static String ref(String gav) {
        String[] coordinates = gav.split(":");
        return "pkg:maven/" + coordinates[0] + "/" + coordinates[1] + "@" + coordinates[2] + "?type=jar";
    }
}
