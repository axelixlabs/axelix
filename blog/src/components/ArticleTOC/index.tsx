/*
 * Copyright (C) 2025-2026 Axelix Labs
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
import { BlogPage } from "@/lib/source";
import { blog } from "@/lib/source";
import { getBaseUrl, withBlogBasePath } from "@/lib/url";
import { getMDXComponents } from "@/mdx-components";

import { TOCProvider, TOCScrollArea } from "fumadocs-ui/components/toc";
import { TOCItem, TOCItems } from "fumadocs-ui/components/toc/default";
import { createRelativeLink } from "fumadocs-ui/mdx";
import Link from "next/link";
import { ReactNode, isValidElement } from "react";

import { BlogShare } from "../BlogShare";

interface IProps {
    page: BlogPage;
    slug: string;
}

/** Recursively extracts the text of a heading's children (for the slug id). */
function extractText(node: ReactNode): string {
    if (typeof node === "string") return node;
    if (typeof node === "number") return String(node);
    if (Array.isArray(node)) return node.map(extractText).join("");
    if (isValidElement(node)) return extractText((node.props as { children?: ReactNode }).children);
    return "";
}

export const ArticleTOC = ({ page, slug }: IProps) => {
    const pageData = page.data;
    const MDX = pageData.body;
    const canonical = new URL(withBlogBasePath(`/${slug}`), getBaseUrl()).toString();

    return (
        <>
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
        </>
    );
};
