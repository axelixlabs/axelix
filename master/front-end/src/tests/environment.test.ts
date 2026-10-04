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

import {
    buildPrecedenceIndex,
    countTriageTag,
    filterPropertySources,
    isFlaggedProperty,
    precedenceChainOf,
    propertyRisks,
    splitProperties,
} from "@/helpers";
import { EDeprecationLevel, EPropertyTriageTag, type IEnvProperty, type IEnvironmentPropertySource } from "@/models";

const namesOf = (properties: IEnvProperty[]): string[] => properties.map(({ name }) => name);

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

    it("Narrows down to the selected triage tag even when the propertySource name matches", () => {
        const result = filterPropertySources(propertySources, "AXELIX_PROPERTY_SOURCE_", [
            EPropertyTriageTag.SUPPRESSED,
        ]);

        expect(result).toHaveLength(1);
        expect(namesOf(result[0].properties)).toEqual(["sun.jnu.encoding"]);
    });

    it("Combines the search with the triage tags, so that both have to match", () => {
        expect(filterPropertySources(propertySources, "specification", [EPropertyTriageTag.SUPPRESSED])).toEqual([]);
    });
});

describe("Precedence chain", () => {
    const property = (name: string, value: string, isPrimary: boolean): IEnvProperty => ({
        name,
        value,
        isPrimary,
        configPropsBeanName: null,
        description: null,
    });

    /**
     * Ordered the way the starter reports them - the highest precedence source first.
     */
    const propertySources: IEnvironmentPropertySource[] = [
        {
            name: "systemEnvironment",
            description: null,
            properties: [property("SPRING_JPA_OPEN_IN_VIEW", "true", true)],
        },
        {
            name: "application-prod.properties",
            description: null,
            properties: [property("spring.jpa.open-in-view", "false", false)],
        },
        {
            name: "application.properties",
            description: null,
            properties: [
                property("spring.jpa.open-in-view", "false", false),
                property("spring.thymeleaf.mode", "HTML", true),
            ],
        },
    ];

    const precedenceIndex = buildPrecedenceIndex(propertySources);

    it("Collects every occurrence of a property across the sources, highest precedence first", () => {
        expect(precedenceChainOf(precedenceIndex, "spring.jpa.open-in-view")).toEqual([
            { propertySourceName: "systemEnvironment", value: "true" },
            { propertySourceName: "application-prod.properties", value: "false" },
            { propertySourceName: "application.properties", value: "false" },
        ]);
    });

    it("Indexes relaxed-binding variants of a name onto the same chain", () => {
        expect(precedenceChainOf(precedenceIndex, "SPRING_JPA_OPEN_IN_VIEW")).toEqual(
            precedenceChainOf(precedenceIndex, "spring.jpa.open-in-view"),
        );
    });

    it("Returns an empty chain for a property that is defined nowhere", () => {
        expect(precedenceChainOf(precedenceIndex, "spring.not.there")).toEqual([]);
    });

    it("Gives a property a drop-down once more than one source defines it", () => {
        const [withDropDown, withoutDropDown] = splitProperties(propertySources[2].properties, precedenceIndex);

        expect(namesOf(withDropDown)).toEqual(["spring.jpa.open-in-view"]);
        expect(namesOf(withoutDropDown)).toEqual(["spring.thymeleaf.mode"]);
    });

    it("Counts the suppressed properties across all the sources", () => {
        expect(countTriageTag(propertySources, EPropertyTriageTag.SUPPRESSED)).toBe(2);
        expect(countTriageTag(propertySources, EPropertyTriageTag.DEPRECATED_WARNING)).toBe(0);
    });
});

describe("Triage classification", () => {
    const base = (name: string): IEnvProperty => ({
        name,
        value: "x",
        isPrimary: true,
        configPropsBeanName: null,
        description: null,
    });

    const dangerous: IEnvProperty = {
        ...base("spring.h2.console.enabled"),
        dangerousValue: { rationale: "Unauthenticated SQL console", alternativeExample: "false" },
    };
    const depError: IEnvProperty = {
        ...base("spring.datasource.initialization-mode"),
        deprecation: { message: "Removed", level: EDeprecationLevel.ERROR, replacedBy: "spring.sql.init.mode" },
    };
    const depWarn: IEnvProperty = {
        ...base("spring.redis.host"),
        deprecation: { message: "Relocated", level: EDeprecationLevel.WARNING, replacedBy: "spring.data.redis.host" },
    };
    const depNoLevel: IEnvProperty = {
        ...base("management.metrics.export.prometheus.enabled"),
        deprecation: { message: "Renamed" },
    };
    const dangerousAndDeprecated: IEnvProperty = {
        ...base("spring.jpa.open-in-view"),
        dangerousValue: { rationale: "Open Session In View", alternativeExample: "false" },
        deprecation: { message: "Legacy", level: EDeprecationLevel.WARNING },
    };

    const propertySources: IEnvironmentPropertySource[] = [
        {
            name: "application.properties",
            description: null,
            properties: [dangerous, depError, depWarn, depNoLevel],
        },
    ];

    it("Counts each risk category independently", () => {
        expect(countTriageTag(propertySources, EPropertyTriageTag.DANGEROUS)).toBe(1);
        expect(countTriageTag(propertySources, EPropertyTriageTag.DEPRECATED_ERROR)).toBe(1);
        expect(countTriageTag(propertySources, EPropertyTriageTag.DEPRECATED_WARNING)).toBe(2);
    });

    it("Treats a deprecation with no stated level as a warning", () => {
        expect(propertyRisks(depNoLevel)).toEqual([
            { kind: EPropertyTriageTag.DEPRECATED_WARNING, reason: "Renamed", hint: null },
        ]);
    });

    it("Lists the dangerous risk before the deprecation when a property carries both", () => {
        expect(propertyRisks(dangerousAndDeprecated).map((risk) => risk.kind)).toEqual([
            EPropertyTriageTag.DANGEROUS,
            EPropertyTriageTag.DEPRECATED_WARNING,
        ]);
    });

    it("Flags a property that carries any risk", () => {
        expect(isFlaggedProperty(dangerous)).toBe(true);
        expect(isFlaggedProperty(depWarn)).toBe(true);
        expect(isFlaggedProperty(base("plain.property"))).toBe(false);
    });
});
