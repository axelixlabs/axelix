import { createI18nMiddleware } from "fumadocs-core/i18n/middleware";
import { isMarkdownPreferred, rewritePath } from "fumadocs-core/negotiation";
import { NextFetchEvent, NextRequest, NextResponse } from "next/server";

import { i18n } from "@/lib/i18n";
import { DOCS_CONTENT_ROUTE, DOCS_IMAGE_ROUTE } from "@/lib/shared";

// docs are mounted at the root of `app/[lang]`, so every page path is a candidate
const { rewrite: rewriteDocs } = rewritePath("{/*path}", `${DOCS_CONTENT_ROUTE}{/*path}/content.md`);
const { rewrite: rewriteSuffix } = rewritePath("{/*path}.md", `${DOCS_CONTENT_ROUTE}{/*path}/content.md`);

// TODO: wrapper — layers custom routing in front of Fumadocs' i18n middleware.
const i18nProxy = createI18nMiddleware(i18n);
// a `Set` (rather than `i18n.languages.includes`) so the lookup accepts a plain `string`
const locales = new Set<string>(i18n.languages);

// TODO: custom — request negotiation (Markdown vs HTML, reserved routes), not built into Fumadocs.
// route handlers now share the page namespace, so they must not be negotiated as docs
export const reservedRoutes = [DOCS_CONTENT_ROUTE, DOCS_IMAGE_ROUTE, "/llms.txt", "/llms-full.txt"];

/**
 * Whether a path belongs to a route handler rather than to a documentation page.
 *
 * @param path - the request path with the locale segment already removed
 * @returns `true` when the path must not be rewritten to a content route
 */
export function isReserved(path: string) {
    return reservedRoutes.some((route) => path === route || path.startsWith(`${route}/`));
}

/**
 * Split a request path into its locale and the remainder:
 * `/ru/x` -> `ru` + `/x`, `/x` -> the default locale + `/x`.
 *
 * @param pathname - the request path, with `basePath` already stripped by Next.js
 * @returns the locale that owns the request and the path without its prefix
 */
export function splitLocale(pathname: string) {
    const [, first, ...rest] = pathname.split("/");

    return locales.has(first)
        ? { locale: first, path: `/${rest.join("/")}` }
        : { locale: i18n.defaultLanguage, path: pathname };
}

/**
 * Resolve the content route that should answer a request for Markdown, if any.
 *
 * @param request - the incoming request, read for its `Accept` header
 * @param path - the request path with the locale segment already removed
 * @returns the content path and the headers it needs, or `undefined` to fall through
 */
function resolveMarkdown(request: NextRequest, path: string) {
    if (isReserved(path)) return undefined;

    // an `.md` suffix is an explicit request, so the response does not vary by `Accept`
    const suffixed = rewriteSuffix(path);
    if (suffixed) return { path: suffixed };

    if (!isMarkdownPreferred(request)) return undefined;

    const negotiated = rewriteDocs(path);
    // this URL has two representations, selected by `Accept`
    return negotiated ? { path: negotiated, headers: { Vary: "Accept" } } : undefined;
}

export default function proxy(request: NextRequest, event: NextFetchEvent) {
    // `pathname` has `basePath` stripped, but a rewrite target must carry it again
    const { pathname, basePath } = request.nextUrl;
    const { locale, path } = splitLocale(pathname);
    const markdown = resolveMarkdown(request, path);

    if (!markdown) return i18nProxy(request, event);

    // content routes live under `app/[lang]`, so the rewrite has to carry the locale itself
    return NextResponse.rewrite(new URL(`${basePath}/${locale}${markdown.path}`, request.nextUrl), {
        headers: markdown.headers,
    });
}

export const config = {
    // `'/'` is listed separately: the catch-all below does not match an empty path,
    // which is what the docs root becomes once `basePath` is stripped. `img` is the
    // `public/` folder and Next metadata files — static assets must not be given a locale prefix.
    matcher: ["/", "/((?!api|_next|img|favicon.ico|icon.svg).*)"],
};
