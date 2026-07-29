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
import { TBlogPage } from "@/models";
import { blog } from "@/lib/source";
import { getBaseUrl, withBlogBasePath } from "@/lib/url";
import { getMDXComponents } from "@/mdx-components";

import { ArticleHeading2 } from "./ArticleHeading2";
import { TOCProvider, TOCScrollArea } from "fumadocs-ui/components/toc";
import { TOCItem, TOCItems } from "fumadocs-ui/components/toc/default";
import { createRelativeLink } from "fumadocs-ui/mdx";

import { ArticleFooter } from "./ArticleFooter";
import styles from "./styles.module.css";

interface IProps {
    page: TBlogPage;
    slug: string;
}

export const Article = ({ page, slug }: IProps) => {
    const pageData = page.data;
    const MDX = pageData.body;
    const canonical = new URL(withBlogBasePath(`/${slug}`), getBaseUrl()).toString();

    return (
        <>
            <TOCProvider toc={pageData.toc}>
                <div className={styles.ArticleContainer}>
                    <article className={styles.ArticleContentWrapper}>
                        <MDX
                            components={getMDXComponents({
                                a: createRelativeLink(blog, page),
                                h2: ArticleHeading2,
                            })}
                        />

                        <ArticleFooter url={canonical} title={pageData.title} />
                    </article>
                    <aside className={`max-md:hidden ${styles.Toc}`}>
                        <span>On this page</span>
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
