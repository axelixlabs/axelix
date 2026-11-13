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

import { filterConfigPropsBeans } from "@/helpers";
import type { IConfigPropsBean } from "@/models";

describe("Filter configProps", () => {
    const beans: IConfigPropsBean[] = [
        {
            beanName: "transaction-org.springframework.boot.autoconfigure.transaction.TransactionProperties",
            prefix: "mappingResources",
            properties: [],
        },
        {
            beanName: "jpa.orm.jpa.JpaProperties",
            prefix: "openInView.showSql",
            properties: [
                {
                    key: "oi",
                    value: "jackson",
                },
                {
                    key: "generateDdl",
                    value: "true",
                },
            ],
        },
    ];

    it("Returns an empty array if configProps beans is empty", () => {
        const result = filterConfigPropsBeans([], "Random search text");
        expect(result).toEqual([]);
    });

    it("A match by the configProps beanName (partially entered) - returns the original configProps bean object", () => {
        const result = filterConfigPropsBeans(beans, "       jpa.orm.jpa.         ");
        expect(result).toHaveLength(1);
        const findedConfigBean = result[0];
        expect(findedConfigBean).toBe(beans[1]);
    });

    it("A match by the configProps prefix (partially entered)", () => {
        const result = filterConfigPropsBeans(beans, "openInView.---       ");
        expect(result).toHaveLength(1);
        const findedConfigBean = result[0];
        expect(findedConfigBean).toEqual({
            beanName: "jpa.orm.jpa.JpaProperties",
            prefix: "openInView.showSql",
            properties: [
                {
                    key: "oi",
                    value: "jackson",
                },
                {
                    key: "generateDdl",
                    value: "true",
                },
            ],
        });
    });

    it("A match by the configProps prefix + property key (partially entered)", () => {
        const result = filterConfigPropsBeans(beans, "openInView.--------showSql.!!!!!!!!!!!!!!generate       ");
        expect(result).toHaveLength(1);
        const findedConfigBean = result[0];
        expect(findedConfigBean).toEqual({
            beanName: "jpa.orm.jpa.JpaProperties",
            prefix: "openInView.showSql",
            properties: [
                {
                    key: "generateDdl",
                    value: "true",
                },
            ],
        });
    });

    it("A match by the configProps property key (partially entered)", () => {
        const result = filterConfigPropsBeans(beans, "!!!!!!!!!!!!!!generate       ");
        expect(result).toHaveLength(1);
        const findedConfigBean = result[0];
        expect(findedConfigBean).toEqual({
            beanName: "jpa.orm.jpa.JpaProperties",
            prefix: "openInView.showSql",
            properties: [
                {
                    key: "generateDdl",
                    value: "true",
                },
            ],
        });
    });

    it("If nothing is found, returns an empty array", () => {
        const result = filterConfigPropsBeans(beans, "zzz-not-found");
        expect(result).toEqual([]);
    });
});

describe("Dangerous configProps filter", () => {
    const dangerous = {
        key: "openInView",
        value: "true",
        dangerousValue: { rationale: "OSIV is enabled", alternativeExample: "false" },
    };
    const beans: IConfigPropsBean[] = [
        { beanName: "jpa", prefix: "spring.jpa", properties: [dangerous, { key: "showSql", value: "false" }] },
        { beanName: "server", prefix: "server", properties: [{ key: "port", value: "8080" }] },
    ];

    it("Keeps only dangerous properties and removes beans without matches", () => {
        // given.
        const search = "";

        // when.
        const result = filterConfigPropsBeans(beans, search, true);

        // then.
        expect(result).toEqual([{ ...beans[0], properties: [dangerous] }]);
    });

    it("Applies the risk filter even when the bean name matches", () => {
        // given.
        const search = "jpa";

        // when.
        const result = filterConfigPropsBeans(beans, search, true);

        // then.
        expect(result).toEqual([{ ...beans[0], properties: [dangerous] }]);
    });

    it("Combines the search and dangerous filter", () => {
        // given.
        const search = "showSql";

        // when.
        const result = filterConfigPropsBeans(beans, search, true);

        // then.
        expect(result).toEqual([]);
    });
});
