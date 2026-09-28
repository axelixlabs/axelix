import { unstable_doesMiddlewareMatch } from "next/experimental/testing/server";
import { readdirSync } from "node:fs";
import { join, relative, sep } from "node:path";
import { fileURLToPath } from "node:url";
import { describe, expect, it } from "vitest";

import { config, isReserved, reservedRoutes } from "../proxy";

const srcDir = fileURLToPath(new URL("..", import.meta.url));
const langDir = join(srcDir, "app", "[lang]");

/** `og/docs/[...slug]/route.tsx` -> `/og/docs` */
function toUrlPath(file: string) {
    const segments = relative(langDir, file)
        .split(sep)
        .slice(0, -1)
        .filter((segment) => !/^\(.*\)$/.test(segment))
        .filter((segment) => !/^\[.*\]$/.test(segment));

    return `/${segments.join("/")}`;
}

const handlers = readdirSync(langDir, { recursive: true, withFileTypes: true })
    .filter((entry) => entry.isFile() && /^route\.tsx?$/.test(entry.name))
    .map((entry) => {
        const file = join(entry.parentPath, entry.name);

        return { file: relative(srcDir, file), urlPath: toUrlPath(file) };
    });

describe("reservedRoutes", () => {
    it("covers every route handler under app/[lang]", () => {
        const uncovered = handlers.filter(({ urlPath }) => !isReserved(urlPath));

        expect(
            uncovered,
            "Route handlers not covered by `reservedRoutes` in src/proxy.ts:\n" +
                uncovered.map(({ file, urlPath }) => `  src/${file}  ->  ${urlPath}`).join("\n") +
                "\nDocs are mounted at the root of app/[lang], so markdown negotiation matches these\n" +
                "paths too. Add them to `reservedRoutes`, or the proxy will rewrite them into\n" +
                "content URLs (a 404, or a self-referential rewrite for the content route itself).",
        ).toEqual([]);
    });

    it("lists no route that no longer exists", () => {
        const served = new Set(handlers.map(({ urlPath }) => urlPath));
        const stale = reservedRoutes.filter((route) => !served.has(route));

        expect(
            stale,
            "`reservedRoutes` in src/proxy.ts lists paths with no route handler under app/[lang]:\n" +
                stale.map((route) => `  ${route}`).join("\n") +
                "\nThe route was renamed or removed — drop the entry.",
        ).toEqual([]);
    });
});

describe("proxy matcher", () => {
    const nextConfig = { basePath: "/docs" };

    it("does not localize the generated app icon", () => {
        expect(
            unstable_doesMiddlewareMatch({
                config,
                nextConfig,
                url: "/docs/icon.svg",
            }),
        ).toBe(false);
    });

    it("still localizes documentation pages", () => {
        expect(
            unstable_doesMiddlewareMatch({
                config,
                nextConfig,
                url: "/docs/product/introduction",
            }),
        ).toBe(true);
    });
});
