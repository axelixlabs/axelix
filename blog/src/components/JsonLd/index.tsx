import { BlogPage } from "@/lib/source";
import { getBlogPostingJsonLd } from "@/lib/structured-data";

interface IProps {
    data: BlogPage["data"];
    slug: string;
}

export const JsonLd = ({ data, slug }: IProps) => {
    const description = data.metaDescription ?? data.description ?? "";
    const jsonLd = getBlogPostingJsonLd({
        title: data.title,
        description,
        slug: slug,
        date: data.date.toISOString(),
        authors: data.authors,
    });

    const jsonLdHtml = JSON.stringify(jsonLd)
        .replace(/</g, "\\u003c")
        .replace(/>/g, "\\u003e")
        .replace(/&/g, "\\u0026");

    return (
        <script
            type="application/ld+json"
            dangerouslySetInnerHTML={{ __html: jsonLdHtml }}
        />
    )
}