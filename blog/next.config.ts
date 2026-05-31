import type { NextConfig } from "next";
import { createMDX } from "fumadocs-mdx/next";

const withMDX = createMDX();

// Basic security headers. A full CSP is deferred until the integrations
// (search, newsletter, analytics) land — the rest of the repo ships none today.
const securityHeaders = [
  { key: "X-Frame-Options", value: "SAMEORIGIN" },
  { key: "X-Content-Type-Options", value: "nosniff" },
  { key: "Referrer-Policy", value: "strict-origin-when-cross-origin" },
];

const config: NextConfig = {
  reactStrictMode: true,
  // Self-contained server output for Docker — emits `.next/standalone/server.js`.
  // NOTE: its dependency file-tracing can hang `next build` in this sandbox;
  // build it in real CI/local, not here.
  output: "standalone",
  basePath: "/blog",
  images: { unoptimized: true },
  // Import `.svg` files as React components (SVGR), e.g. the logo.
  turbopack: {
    rules: {
      "*.svg": {
        loaders: ["@svgr/webpack"],
        as: "*.js",
      },
    },
  },
  async headers() {
    return [{ source: "/:path*", headers: securityHeaders }];
  },
};

export default withMDX(config);
