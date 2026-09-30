import { createMDX } from "fumadocs-mdx/next";

import { BASE_PATH, prefixedLocales } from "./src/lib/constants.mjs";

const withMDX = createMDX();

/** @type {import('next').NextConfig} */
const config = {
    basePath: BASE_PATH,
    reactStrictMode: true,
    output: "standalone",
    // `source` and `destination` are prefixed with `basePath` automatically.
    // Not permanent (308): if "1.x" ever becomes a real archived version again, this
    // redirect gets replaced by actual content, and a browser must not have cached it forever.
    // TODO: custom — backward-compat redirects, not a Fumadocs feature.
    async redirects() {
        return [
            // URLs shared back when content lived under the "1.x" version segment keep working
            { source: "/1.x/:path*", destination: "/:path*", permanent: false },
            ...prefixedLocales.map((locale) => ({
                source: `/${locale}/1.x/:path*`,
                destination: `/${locale}/:path*`,
                permanent: false,
            })),
        ];
    },
};

export default withMDX(config);
