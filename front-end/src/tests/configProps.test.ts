import { describe, expect, it } from "vitest";

import { filterConfigPropsBeans } from "helpers";
import type { IConfigPropsBean } from "models";

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
