import { createMDX } from "fumadocs-mdx/next";

import { BASE_PATH, LATEST_VERSION, prefixedLocales, versions } from "./src/lib/constants.mjs";

const withMDX = createMDX();

// First segments that must never be versioned: route handlers, static assets, and every
// version itself — without the latter, an already-versioned URL would be versioned again.
const notAPage = [
    ...versions.map((version) => version.replaceAll(".", "\\.")),
    "api",
    "og",
    "llms",
    "img",
    "icon\\.svg",
].join("|");

/** @type {import('next').NextConfig} */
const config = {
    basePath: BASE_PATH,
    reactStrictMode: true,
    // `source` and `destination` are prefixed with `basePath` automatically.
    // These all point at whichever version is current, which moves over time,
    // so they are 307 rather than 308 — a browser must not cache them forever.
    async redirects() {
        return [
            // the docs root opens the current version
            { source: "/", destination: `/${LATEST_VERSION}`, permanent: false },
            ...prefixedLocales.map((locale) => ({
                source: `/${locale}`,
                destination: `/${locale}/${LATEST_VERSION}`,
                permanent: false,
            })),
            // URLs from before versioning keep working
            ...prefixedLocales.map((locale) => ({
                source: `/${locale}/:path((?!${notAPage}).*)`,
                destination: `/${locale}/${LATEST_VERSION}/:path`,
                permanent: false,
            })),
            {
                source: `/:path((?!${notAPage}|${prefixedLocales.join("|")}).*)`,
                destination: `/${LATEST_VERSION}/:path`,
                permanent: false,
            },
        ];
    },
};

export default withMDX(config);
