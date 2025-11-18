import { describe, expect, it } from "vitest";

import { filterPropertySources } from "helpers";
import type { IEnvironmentPropertySource } from "models";

describe("Filter propertySources", () => {
    const propertySources: IEnvironmentPropertySource[] = [
        {
            name: "server.ports",
            properties: [],
        },
        {
            name: "AXILE_PROPERTY_SOURCE_NAME",
            properties: [
                {
                    name: "java.specification.version",
                    value: "17",
                    isPrimary: true,
                    configPropsBeanName: null,
                },
                {
                    name: "sun.jnu.encoding",
                    value: "UTF-8",
                    isPrimary: false,
                    configPropsBeanName: null,
                },
            ],
        },
    ];

    it("Returns an empty array if propertySources is empty", () => {
        const result = filterPropertySources([], "Random search text");
        expect(result).toEqual([]);
    });

    it("A match by the propertySource name (partially entered) - returns the original propertySource object", () => {
        const result = filterPropertySources(propertySources, "            AXILE_PROPERTY_SOURCE_           ");
        expect(result).toHaveLength(1);
        expect(result[0]).toBe(propertySources[1]);
    });

    it("Match by property name (partially entered) - returns the propertySource with filtered properties", () => {
        const result = filterPropertySources(propertySources, "       specification.---..version!!!?????****       ");
        expect(result).toHaveLength(1);
        const findedPropertySource = result[0];
        expect(findedPropertySource.name).toBe(propertySources[1].name);
        expect(findedPropertySource.properties).toEqual([
            { name: "java.specification.version", value: "17", isPrimary: true, configPropsBeanName: null },
        ]);
    });

    it("If nothing is found, returns an empty array", () => {
        const result = filterPropertySources(propertySources, "zzz-not-found");
        expect(result).toEqual([]);
    });
});
