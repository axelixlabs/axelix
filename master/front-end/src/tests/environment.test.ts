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
import { describe, expect, it } from "vitest";

import { filterPropertySources } from "helpers";
import type { IEnvironmentPropertySource } from "models";

describe("Filter propertySources", () => {
    const propertySources: IEnvironmentPropertySource[] = [
        {
            name: "server.ports",
            description: null,
            properties: [],
        },
        {
            name: "AXELIX_PROPERTY_SOURCE_NAME",
            description: null,
            properties: [
                {
                    name: "java.specification.version",
                    value: "17",
                    isPrimary: true,
                    configPropsBeanName: null,
                    description: null,
                },
                {
                    name: "sun.jnu.encoding",
                    value: "UTF-8",
                    isPrimary: false,
                    configPropsBeanName: null,
                    description: null,
                },
            ],
        },
    ];

    it("Returns an empty array if propertySources is empty", () => {
        const result = filterPropertySources([], "Random search text");
        expect(result).toEqual([]);
    });

    it("A match by the propertySource name (partially entered) - returns the original propertySource object", () => {
        const result = filterPropertySources(propertySources, "            AXELIX_PROPERTY_SOURCE_           ");
        expect(result).toHaveLength(1);
        expect(result[0]).toBe(propertySources[1]);
    });

    it("Match by property name (partially entered) - returns the propertySource with filtered properties", () => {
        const result = filterPropertySources(propertySources, "       specification.---..version!!!?????****       ");
        expect(result).toHaveLength(1);
        const findedPropertySource = result[0];
        expect(findedPropertySource.name).toBe(propertySources[1].name);
        expect(findedPropertySource.properties).toEqual([
            {
                name: "java.specification.version",
                value: "17",
                isPrimary: true,
                configPropsBeanName: null,
                description: null,
            },
        ]);
    });

    it("If nothing is found, returns an empty array", () => {
        const result = filterPropertySources(propertySources, "zzz-not-found");
        expect(result).toEqual([]);
    });
});
