import { beforeAll, describe, expect, it } from "vitest";

import config from "../../next.config.mjs";
import { LATEST_VERSION, prefixedLocales, versions } from "../lib/constants.mjs";

type Redirect = { source: string; destination: string; permanent: boolean };

let redirects: Redirect[];

beforeAll(async () => {
    if (!config.redirects) throw new Error("next.config.mjs declares no redirects");

    redirects = (await config.redirects()) as Redirect[];
});

/**
 * These assert the shape of the rules, not their matching — the matching itself is
 * covered by the curl matrix run against a production build.
 */
describe("version redirects", () => {
    it("sends both locale roots to the current version", () => {
        expect(redirects).toContainEqual({ source: "/", destination: `/${LATEST_VERSION}`, permanent: false });

        for (const locale of prefixedLocales) {
            expect(redirects).toContainEqual({
                source: `/${locale}`,
                destination: `/${locale}/${LATEST_VERSION}`,
                permanent: false,
            });
        }
    });

    it("keeps the locale ahead of the version", () => {
        // the public URL order is basePath, then locale, then version
        for (const locale of prefixedLocales) {
            const rule = redirects.find((it) => it.source.startsWith(`/${locale}/:path`));

            expect(rule?.destination).toBe(`/${locale}/${LATEST_VERSION}/:path`);
        }
    });

    it("carries the matched path over whole", () => {
        for (const rule of redirects.filter((it) => it.source.includes(":path"))) {
            expect(rule.destination).toContain(":path");
        }
    });

    it("never redirects onto another origin", () => {
        for (const rule of redirects) {
            expect(rule.destination).toMatch(new RegExp(`^/(?:${prefixedLocales.join("|")}/)?${"\\d"}`));
        }
    });

    it("excludes already-versioned paths, so there is no loop", () => {
        for (const version of versions) {
            const escaped = version.replaceAll(".", "\\.");

            for (const rule of redirects.filter((it) => it.source.includes(":path"))) {
                expect(rule.source).toContain(escaped);
            }
        }
    });

    it("excludes every route handler and asset path that shares the page namespace", () => {
        for (const rule of redirects.filter((it) => it.source.includes(":path"))) {
            for (const reserved of ["api", "og", "llms", "img", "icon\\.svg"]) {
                expect(rule.source).toContain(reserved);
            }
        }
    });

    it("is temporary, because the current version moves", () => {
        expect(redirects.every((rule) => rule.permanent === false)).toBe(true);
    });
});
