import { notFound } from "next/navigation";
import { ImageResponse } from "next/og";

import { OG_IMAGE_HEIGHT, OG_IMAGE_WIDTH, OgImage } from "@/components/OgImage";
import { loadOgAssets } from "@/lib/og-assets";
import { BASE_PATH, getPageImageUrl } from "@/lib/shared";
import { siteOrigin, source } from "@/lib/source";

export const revalidate = false;
// only the pre-rendered images exist: the last slug segment is not checked, so an
// arbitrary file name must not render (and cache) an image on demand
export const dynamicParams = false;

// printed without the scheme, e.g. `axelix.io/docs/start`
const siteHost = new URL(siteOrigin).host;

/**
 * The address of a page as shown on its OG image.
 *
 * @param pageUrl - the page URL from the loader, without `basePath`
 * @returns the host, `basePath` and page path, without a trailing slash
 */
function getDisplayUrl(pageUrl: string) {
    return `${siteHost}${BASE_PATH}${pageUrl === "/" ? "" : pageUrl}`;
}

export async function GET(_req: Request, { params }: RouteContext<"/[lang]/og/docs/[...slug]">) {
    const { lang, slug } = await params;
    const page = source.getPage(slug.slice(0, -1), lang);
    if (!page) notFound();

    const { fonts, logoSrc } = await loadOgAssets();

    return new ImageResponse(
        <OgImage
            title={page.data.title}
            description={page.data.description}
            url={getDisplayUrl(page.url)}
            logoSrc={logoSrc}
        />,
        { width: OG_IMAGE_WIDTH, height: OG_IMAGE_HEIGHT, fonts },
    );
}

export function generateStaticParams() {
    return source.getPages().map((page) => ({
        lang: page.locale,
        slug: getPageImageUrl(page).segments,
    }));
}
