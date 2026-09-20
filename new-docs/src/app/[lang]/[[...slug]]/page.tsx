import {
    DocsBody,
    DocsDescription,
    DocsPage,
    DocsTitle,
    MarkdownCopyButton,
    ViewOptionsPopover,
} from "fumadocs-ui/layouts/notebook/page";
import { createRelativeLink } from "fumadocs-ui/mdx";
import type { Metadata } from "next";
import { notFound } from "next/navigation";

import { getMDXComponents } from "@/components/mdx";
import { DEFAULT_LOCALE, prefixedLocales } from "@/lib/constants.mjs";
import { getPageImageUrl, getPageMarkdownUrl, gitConfig } from "@/lib/shared";
import { source } from "@/lib/source";

// Includes `BASE_PATH`, so paths handed to `alternates` are written without it.
const siteUrl = new URL(process.env.SITE_URL ?? "https://axelix.io/docs");

/**
 * A locale with no file of its own falls back to the default locale, and both
 * pages then share the same source file — advertising `hreflang` for it would
 * promise a translation that does not exist.
 */
function getLanguageAlternates(slug: string[] | undefined) {
    const defaultPage = source.getPage(slug, DEFAULT_LOCALE);
    if (!defaultPage) return undefined;

    const languages: Record<string, string> = {
        [DEFAULT_LOCALE]: defaultPage.url,
        "x-default": defaultPage.url,
    };

    for (const locale of prefixedLocales) {
        const page = source.getPage(slug, locale);

        if (page && page.path !== defaultPage.path) languages[locale] = page.url;
    }

    return languages;
}

export default async function Page(props: PageProps<"/[lang]/[[...slug]]">) {
    const params = await props.params;
    const page = source.getPage(params.slug, params.lang);
    if (!page) notFound();

    const MDX = page.data.body;
    const markdownUrl = getPageMarkdownUrl(page).url;

    return (
        <DocsPage
            toc={page.data.toc}
            full={page.data.full}
            breadcrumb={{ enabled: false }}
            tableOfContent={{ container: { className: "pt-8" } }}
            className="xl:pt-8"
        >
            <div className="flex flex-col gap-4 sm:flex-row sm:items-start sm:justify-between sm:gap-6">
                <DocsTitle>{page.data.title}</DocsTitle>

                <div className="flex shrink-0 items-center gap-2">
                    <MarkdownCopyButton markdownUrl={markdownUrl} />
                    <ViewOptionsPopover
                        markdownUrl={markdownUrl}
                        githubUrl={`https://github.com/${gitConfig.user}/${gitConfig.repo}/blob/${gitConfig.branch}/content/docs/${page.path}`}
                    />
                </div>
            </div>
            <DocsDescription className="mb-6">{page.data.description}</DocsDescription>
            <DocsBody>
                <MDX
                    components={getMDXComponents({
                        // this allows you to link to other pages with relative file paths
                        a: createRelativeLink(source, page),
                    })}
                />
            </DocsBody>
        </DocsPage>
    );
}

export async function generateStaticParams() {
    return source.generateParams();
}

export async function generateMetadata(props: PageProps<"/[lang]/[[...slug]]">): Promise<Metadata> {
    const params = await props.params;
    const page = source.getPage(params.slug, params.lang);
    if (!page) notFound();

    return {
        metadataBase: siteUrl,
        title: page.data.title,
        description: page.data.description,
        alternates: {
            canonical: page.url,
            languages: getLanguageAlternates(params.slug),
        },
        openGraph: {
            // already carries `BASE_PATH`, so it is resolved against the origin only
            images: new URL(getPageImageUrl(page).url, siteUrl.origin).toString(),
        },
    };
}
