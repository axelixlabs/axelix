import { beforeAll, describe, expect, it } from "vitest";

import config from "../../next.config.mjs";
import { prefixedLocales } from "../lib/constants.mjs";

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
describe("legacy version redirects", () => {
    it("sends URLs shared back when content lived under /1.x to the unversioned path", () => {
        expect(redirects).toContainEqual({
            source: "/1.x/:path*",
            destination: "/:path*",
            permanent: false,
        });

        for (const locale of prefixedLocales) {
            expect(redirects).toContainEqual({
                source: `/${locale}/1.x/:path*`,
                destination: `/${locale}/:path*`,
                permanent: false,
            });
        }
    });

    it("keeps the locale ahead of the legacy version segment", () => {
        for (const locale of prefixedLocales) {
            const rule = redirects.find((it) => it.source.startsWith(`/${locale}/1.x`));

            expect(rule?.destination).toBe(`/${locale}/:path*`);
        }
    });

    it("carries the matched path over whole", () => {
        for (const rule of redirects) {
            expect(rule.destination).toContain(":path*");
        }
    });

    it("is temporary, in case /1.x becomes a real archived version again", () => {
        expect(redirects.every((rule) => rule.permanent === false)).toBe(true);
    });
});
