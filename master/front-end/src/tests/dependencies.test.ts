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

import { buildResolutionPath, buildSupportTimeline, filterDependencies, isDirectDependency } from "@/helpers";
import {
    EDependencyEcosystem,
    EPlatformSupportStatus,
    ESupportSignal,
    type IPlatformSupportWindow,
    type IResolvedDependency,
} from "@/models";

const dependency = (overrides: Partial<IResolvedDependency>): IResolvedDependency => ({
    coordinates: "org.springframework:spring-webmvc",
    project: "Spring Framework",
    version: "6.1.5",
    ecosystem: EDependencyEcosystem.SPRING,
    signal: null,
    resolutionPath: ["org.springframework:spring-webmvc:6.1.5"],
    status: "Actively developed.",
    referenceLabel: "Spring Framework reference",
    referenceUrl: "https://docs.spring.io/spring-framework/reference/",
    ...overrides,
});

const SPRING = dependency({});

const HYSTRIX = dependency({
    coordinates: "com.netflix.hystrix:hystrix-core",
    project: "Hystrix",
    ecosystem: EDependencyEcosystem.RESILIENCE,
    signal: {
        kind: ESupportSignal.DISCONTINUED,
        reason: "Stopped active development.",
        evidence: "Curated project registry",
        note: null,
    },
    resolutionPath: ["com.acme:platform-bom:4.2.0", "com.netflix.hystrix:hystrix-core:1.5.18"],
});

const JODA = dependency({
    coordinates: "joda-time:joda-time",
    project: "Joda-Time",
    ecosystem: EDependencyEcosystem.OTHER,
    signal: {
        kind: ESupportSignal.MAINTENANCE,
        reason: "Superseded by java.time.",
        evidence: "Curated project registry",
        note: null,
    },
});

const ALL = [SPRING, HYSTRIX, JODA];

describe("Check filterDependencies function", () => {
    it("Returns every dependency when nothing is filtered", () => {
        expect(filterDependencies(ALL, "", null, [])).toEqual(ALL);
    });

    it("Matches the search against coordinates and project name, ignoring separators", () => {
        expect(filterDependencies(ALL, "springwebmvc", null, [])).toEqual([SPRING]);
        expect(filterDependencies(ALL, "jodatime", null, [])).toEqual([JODA]);
        expect(filterDependencies(ALL, "Hystrix", null, [])).toEqual([HYSTRIX]);
    });

    it("Ignores surrounding whitespace in the search", () => {
        expect(filterDependencies(ALL, "   hystrix  ", null, [])).toEqual([HYSTRIX]);
    });

    it("Keeps only the dependencies of the selected ecosystem", () => {
        expect(filterDependencies(ALL, "", EDependencyEcosystem.SPRING, [])).toEqual([SPRING]);
        expect(filterDependencies(ALL, "", EDependencyEcosystem.LOGGING, [])).toEqual([]);
    });

    it("Keeps only the dependencies carrying one of the active signals", () => {
        expect(filterDependencies(ALL, "", null, [ESupportSignal.DISCONTINUED])).toEqual([HYSTRIX]);
        expect(filterDependencies(ALL, "", null, [ESupportSignal.DISCONTINUED, ESupportSignal.MAINTENANCE])).toEqual([
            HYSTRIX,
            JODA,
        ]);
    });

    it("Applies the search, the ecosystem and the signals together", () => {
        expect(filterDependencies(ALL, "joda", EDependencyEcosystem.OTHER, [ESupportSignal.MAINTENANCE])).toEqual([
            JODA,
        ]);
        expect(filterDependencies(ALL, "joda", EDependencyEcosystem.SPRING, [])).toEqual([]);
    });
});

describe("Check isDirectDependency function", () => {
    it("Returns true for a dependency declared in the build file", () => {
        expect(isDirectDependency(SPRING)).toBe(true);
    });

    it("Returns false for a dependency pulled in by another one", () => {
        expect(isDirectDependency(HYSTRIX)).toBe(false);
    });
});

describe("Check buildResolutionPath function", () => {
    it("Prepends the analyzed application as the root of the chain", () => {
        expect(buildResolutionPath("com.acme:visits-service:4.7.2", SPRING)).toEqual([
            { coordinates: "com.acme:visits-service:4.7.2", depth: 0, root: true, resolved: false },
            { coordinates: "org.springframework:spring-webmvc:6.1.5", depth: 1, root: false, resolved: true },
        ]);
    });

    it("Marks the last hop as the one the resolution lands on", () => {
        const path = buildResolutionPath("com.acme:visits-service:4.7.2", HYSTRIX);

        expect(path).toHaveLength(3);
        expect(path[1]).toEqual({
            coordinates: "com.acme:platform-bom:4.2.0",
            depth: 1,
            root: false,
            resolved: false,
        });
        expect(path[2].resolved).toBe(true);
    });
});

describe("Check buildSupportTimeline function", () => {
    const platform = (overrides: Partial<IPlatformSupportWindow>): IPlatformSupportWindow => ({
        name: "Spring Boot",
        version: "3.2.4",
        line: "3.2.x",
        latestKnownLine: "3.5.x",
        status: EPlatformSupportStatus.OUT_OF_OSS_MAINTENANCE,
        releasedAt: "2020-01-01",
        ossSupportEndsAt: "2021-01-01",
        commercialSupportEndsAt: "2023-01-01",
        supportedTargetLine: "3.5.x",
        supportedTargetOssEndsAt: "2027-06-30",
        ...overrides,
    });

    it("Splits the window between the OSS and the commercial-only stretch", () => {
        const timeline = buildSupportTimeline(platform({}));

        expect(timeline.ossPercentage).toBeCloseTo(33.35, 1);
        expect(timeline.ossPercentage + timeline.commercialPercentage).toBe(100);
    });

    it("Treats a line without commercial support as entirely OSS", () => {
        const timeline = buildSupportTimeline(platform({ commercialSupportEndsAt: null }));

        expect(timeline.ossPercentage).toBe(100);
        expect(timeline.commercialPercentage).toBe(0);
    });

    it("Clamps a today that falls outside the window to its bounds", () => {
        expect(buildSupportTimeline(platform({})).todayPercentage).toBe(100);

        const future = platform({
            releasedAt: "2999-01-01",
            ossSupportEndsAt: "3000-01-01",
            commercialSupportEndsAt: "3001-01-01",
        });

        expect(buildSupportTimeline(future).todayPercentage).toBe(0);
    });

    it("Falls back to a full OSS bar when the window has no duration", () => {
        const collapsed = platform({ ossSupportEndsAt: "2020-01-01", commercialSupportEndsAt: "2020-01-01" });

        expect(buildSupportTimeline(collapsed)).toEqual({
            ossPercentage: 100,
            commercialPercentage: 0,
            todayPercentage: 100,
        });
    });
});
