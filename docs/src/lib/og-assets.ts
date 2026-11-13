import { readFile } from "node:fs/promises";
import path from "node:path";

import { OG_FONT_NAME } from "./shared";

/*
 * Static cuts of Golos Text: Satori (behind `next/og`) can parse neither woff2 nor the
 * weight axis of a variable font. They were instanced from `src/app/fonts/variable-golos-text.woff2`
 * with every glyph kept, so that no title character makes `next/og` fetch a fallback font:
 *
 *   fonttools varLib.instancer variable-golos-text.woff2 wght=400 --static -o regular.woff2
 *
 * then re-saved without the woff2 compression (`TTFont.flavor = None`) as
 * `golos-text-regular.ttf`; the same with `wght=500` for `golos-text-medium.ttf`.
 *
 * Paths are literals, so that build tracing picks exactly these files.
 */
async function readAssets() {
    const [regular, medium, logo] = await Promise.all([
        readFile(path.join(process.cwd(), "src/app/fonts/golos-text-regular.ttf")),
        readFile(path.join(process.cwd(), "src/app/fonts/golos-text-medium.ttf")),
        // the variant with white letters, for the dark card
        readFile(path.join(process.cwd(), "public/img/logo-dark.svg")),
    ]);

    return {
        fonts: [
            { name: OG_FONT_NAME, data: regular, weight: 400 as const, style: "normal" as const },
            { name: OG_FONT_NAME, data: medium, weight: 500 as const, style: "normal" as const },
        ],
        /** The Axelix wordmark as a data URL, ready for an `<img>` inside the image. */
        logoSrc: `data:image/svg+xml;base64,${logo.toString("base64")}`,
    };
}

let assetsCache: ReturnType<typeof readAssets> | undefined;

/**
 * Load the fonts and the logo for OG images. Read from disk rather than fetched, since
 * the images are pre-rendered at build time; read once for all of them.
 *
 * TODO: custom — replaces the default font and icon of Fumadocs' `generateOGImage`.
 *
 * @returns the fonts to pass to `ImageResponse` and the logo source
 */
export function loadOgAssets() {
    assetsCache ??= readAssets();

    return assetsCache;
}
