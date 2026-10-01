import type { MetadataRoute } from "next";

import { BASE_PATH, DEFAULT_LOCALE, prefixedLocales } from "@/lib/constants.mjs";
import { source } from "@/lib/source";

// `source` mounts pages at the root (`baseUrl: "/"`); the `/docs` prefix and the
// locale prefix are added here to produce absolute, canonical deployment URLs.
const origin = new URL(process.env.SITE_URL ?? "https://axelix.io").origin;

/**
 * Absolute, canonical URL of a page. Mirrors the canonical written in
 * `generateMetadata` (basePath + locale-prefixed `page.url`, no trailing slash so
 * the docs root resolves to `/docs`, not `/docs/`).
 */
function toAbsolute(pageUrl: string): string {
    const path = `${BASE_PATH}${pageUrl}`.replace(/\/+$/, "") || BASE_PATH;
    return new URL(path, origin).toString();
}

/**
 * hreflang alternates for a page, keyed by locale plus `x-default`. A prefixed
 * locale that falls back to the default source file is omitted — advertising it
 * would promise a translation that does not exist (same guard as `generateMetadata`).
 */
function languageAlternates(slug: string[] | undefined) {
    const defaultPage = source.getPage(slug, DEFAULT_LOCALE);
    if (!defaultPage) return undefined;

    const defaultUrl = toAbsolute(defaultPage.url);
    const languages: Record<string, string> = {
        [DEFAULT_LOCALE]: defaultUrl,
        "x-default": defaultUrl,
    };

    for (const locale of prefixedLocales) {
        const page = source.getPage(slug, locale);
        if (page && page.path !== defaultPage.path) languages[locale] = toAbsolute(page.url);
    }

    // only a real translation (beyond the default + x-default) is worth emitting
    return Object.keys(languages).length > 2 ? { languages } : undefined;
}

export default function sitemap(): MetadataRoute.Sitemap {
    // `getPages()` yields a page per locale (en + ru), so every localized URL is a
    // first-class entry; `languageAlternates` makes their hreflang sets reciprocal.
    return source.getPages().map((page) => ({
        url: toAbsolute(page.url),
        lastModified: page.data.lastModified,
        alternates: languageAlternates(page.slugs),
    }));
}
