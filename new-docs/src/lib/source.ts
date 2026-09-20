import { llms, loader } from "fumadocs-core/source";
import { lucideIconsPlugin } from "fumadocs-core/source/lucide-icons";
import { metaSchema, pageSchema } from "fumadocs-core/source/schema";
import { defineDocs } from "fumadocs-mdx/macro";

import { BASE_PATH, DEFAULT_LOCALE } from "./constants.mjs";
import { i18n } from "./i18n";

const docs = defineDocs({
    dir: "content/docs",
    docs: {
        schema: pageSchema,
        postprocess: {
            includeProcessedMarkdown: true,
        },
    },
    meta: {
        schema: metaSchema,
    },
});

const contentSource = docs.toFumadocsSource();
const siteOrigin = new URL(process.env.SITE_URL ?? "https://axelix.io").origin;

// See https://fumadocs.dev/docs/headless/source-api for more info
export const source = loader({
    i18n,
    // the `/docs` prefix comes from `basePath`, so pages are mounted at the root here
    baseUrl: "/",
    source: contentSource,
    plugins: [lucideIconsPlugin()],
});

/**
 * LLM exports need deployment URLs, while the regular source must keep paths relative to
 * Next's basePath because Next adds that prefix to normal links automatically.
 */
const llmsSource = loader({
    i18n,
    baseUrl: "/",
    source: contentSource,
    url: (slugs, locale) => {
        const localePrefix = locale && locale !== DEFAULT_LOCALE ? `/${locale}` : "";
        return `${siteOrigin}${BASE_PATH}${localePrefix}/${slugs.join("/")}`;
    },
    plugins: [lucideIconsPlugin()],
});

export const docsLlms = llms(llmsSource, {
    renderPage: async (page) => `# ${page.data.title} (${page.url})

${await page.data.getText("processed")}`,
});
