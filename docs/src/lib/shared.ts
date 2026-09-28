import { createGetUrl } from "fumadocs-core/source";

import { BASE_PATH } from "./constants.mjs";
import { i18n } from "./i18n";

/** Site name, shown in the navbar and in generated OG images. */
export const APP_NAME = "Axelix";

export { BASE_PATH };

/** Route handler that renders a page as an OG image. */
export const DOCS_IMAGE_ROUTE = "/og/docs";

/** Route handler that serves the Markdown source of a page. */
export const DOCS_CONTENT_ROUTE = "/llms.mdx/docs";

/** Build a localized URL for a documentation page. */
export const getDocsUrl = createGetUrl("", i18n);

/** Repository the "Edit on GitHub" links point at. */
export const gitConfig = {
    user: "axelixlabs",
    repo: "axelix",
    branch: "master",
};

/** Base URL of the GitHub repository — e.g. for blob/edit/issue links. */
export const GITHUB_REPO_URL = `https://github.com/${gitConfig.user}/${gitConfig.repo}`;

const getContentUrl = createGetUrl(DOCS_CONTENT_ROUTE, i18n);

/**
 * Build the URL of a page's Markdown representation.
 *
 * Fumadocs' `MarkdownCopyButton` and `ViewOptionsPopover` apply `basePath` through
 * their internal `withBasePath()`, so the URL passed to them must not include it.
 *
 * TODO: custom — bespoke Markdown URL helper.
 *
 * @param page - the page to address, as returned by the loader
 * @returns the route segments and the ready-to-use URL
 */
export function getPageMarkdownUrl(page: { slugs: string[]; locale?: string }) {
    const segments = [...page.slugs, "content.md"];

    return { segments, url: getContentUrl(segments, page.locale) };
}

const getImageUrl = createGetUrl(DOCS_IMAGE_ROUTE, i18n);

/**
 * Build the URL of a page's OG image. It is used directly in metadata, where
 * no Fumadocs component adds `basePath`, so the URL must include it here.
 *
 * TODO: custom — bespoke OG-image URL helper.
 *
 * @param page - the page to address, as returned by the loader
 * @returns the route segments and the ready-to-use URL
 */
export function getPageImageUrl(page: { slugs: string[]; locale?: string }) {
    const segments = [...page.slugs, "image.png"];

    return { segments, url: BASE_PATH + getImageUrl(segments, page.locale) };
}
