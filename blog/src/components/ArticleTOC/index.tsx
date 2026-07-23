
import { createRelativeLink } from "fumadocs-ui/mdx";
import { TOCProvider, TOCScrollArea } from "fumadocs-ui/components/toc";
import { TOCItems, TOCItem } from "fumadocs-ui/components/toc/default";
import { getMDXComponents } from "@/mdx-components";
import { BlogPage } from "@/lib/source";
import { getBaseUrl, withBlogBasePath } from "@/lib/url";
import { isValidElement, ReactNode } from "react";
import Link from "next/link";
import { BlogShare } from "../BlogShare";
import { blog } from "@/lib/source";

interface IProps {
    page: BlogPage;
    slug: string;
}

/** Recursively extracts the text of a heading's children (for the slug id). */
function extractText(node: ReactNode): string {
    if (typeof node === "string") return node;
    if (typeof node === "number") return String(node);
    if (Array.isArray(node)) return node.map(extractText).join("");
    if (isValidElement(node))
        return extractText((node.props as { children?: ReactNode }).children);
    return "";
}

export const ArticleTOC = ({ page, slug }: IProps) => {
    const pageData = page.data
    const MDX = pageData.body;
    const canonical = new URL(withBlogBasePath(`/${slug}`), getBaseUrl()).toString();

    return (
        <TOCProvider toc={pageData.toc}>
            <div className="art-layout">
                <article className="prose">
                    <MDX
                        components={getMDXComponents({
                            a: createRelativeLink(blog, page),
                            h2: (props) => {
                                const providedId =
                                    typeof (props as { id?: unknown }).id === "string"
                                        ? ((props as { id?: string }).id ?? "")
                                        : "";
                                const id =
                                    providedId ||
                                    extractText(props.children)
                                        .trim()
                                        .toLowerCase()
                                        .replace(/\s+/g, "-")
                                        .replace(/[^a-z0-9-]/g, "")
                                        .replace(/-+/g, "-")
                                        .replace(/^-|-$/g, "");
                                return (
                                    <h2 id={id} className="group flex scroll-mt-28 items-center gap-2">
                                        <a href={`#${id}`}>{props.children}</a>
                                    </h2>
                                );
                            },
                        })}
                    />
                    <div className="art-foot">
                        <Link className="back-link" href="/">
                            <span className="arr">←</span> All articles
                        </Link>
                        <BlogShare url={canonical} title={pageData.title} />
                    </div>
                </article>
                <aside className="toc max-md:hidden">
                    <span className="toc-title">On this page</span>
                    <TOCScrollArea>
                        <TOCItems>
                            {pageData.toc.map((item) => (
                                <TOCItem key={item.url} item={item} />
                            ))}
                        </TOCItems>
                    </TOCScrollArea>
                </aside>
            </div>
        </TOCProvider>
    )
}