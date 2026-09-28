import { generateOGImage } from "fumadocs-ui/og";
import { notFound } from "next/navigation";

import { APP_NAME, getPageImageUrl } from "@/lib/shared";
import { source } from "@/lib/source";

export const revalidate = false;

export async function GET(_req: Request, { params }: RouteContext<"/[lang]/og/docs/[...slug]">) {
    const { lang, slug } = await params;
    const page = source.getPage(slug.slice(0, -1), lang);
    if (!page) notFound();

    return generateOGImage({
        title: page.data.title,
        description: page.data.description,
        site: APP_NAME,
    });
}

export function generateStaticParams() {
    return source.getPages().map((page) => ({
        lang: page.locale,
        slug: getPageImageUrl(page).segments,
    }));
}
