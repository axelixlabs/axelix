#!/usr/bin/env node
// Usage: node blog/scripts/generate-preview.mjs 1.3.0
// Generates blog/public/posts/axelix-1-3-0/preview.png from the SVG template.

import { readFileSync, mkdirSync } from "fs";
import { join, dirname } from "path";
import { fileURLToPath } from "url";
import sharp from "sharp";

const version = process.argv[2];
if (!version || !/^\d+\.\d+\.\d+$/.test(version)) {
  console.error("Usage: node blog/scripts/generate-preview.mjs <version>  (e.g. 1.3.0)");
  process.exit(1);
}

const root = join(dirname(fileURLToPath(import.meta.url)), "..");

const svg = readFileSync(join(root, "templates", "release-preview.svg"), "utf8")
  .replace(
    /(<text[^>]*id="version"[^>]*>)[^<]*(<\/text>)/,
    `$1${version}$2`
  );

const slug = `axelix-${version.replace(/\./g, "-")}`;
const outDir = join(root, "public", "posts", slug);
mkdirSync(outDir, { recursive: true });

await sharp(Buffer.from(svg))
  .png()
  .toFile(join(outDir, "preview.png"));

console.log(`✓ ${outDir}/preview.png`);
