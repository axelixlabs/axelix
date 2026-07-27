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
import { ArticleHeader, ArticleTOC, JsonLd, ReadProgress } from "@/components";
import { blog } from "@/lib/source";
import { withBlogBasePath } from "@/lib/url";

import type { Metadata } from "next";
import Link from "next/link";
import { notFound } from "next/navigation";

export function generateStaticParams() {
    return blog.getPages().map(({ slugs }) => ({ slug: slugs[0] }));
}

export async function generateMetadata({ params }: { params: Promise<{ slug: string }> }): Promise<Metadata> {
    const { slug } = await params;
    const page = blog.getPage([slug]);
    if (!page) {
        return {};
    }

    const title = page.data.metaTitle ?? page.data.title;
    const description = page.data.metaDescription ?? page.data.description ?? "";
    const canonical = withBlogBasePath(`/${slug}`);

    return {
        title,
        description,
        alternates: { canonical },
        openGraph: { title, description, url: canonical, type: "article" },
        twitter: { card: "summary_large_image", title, description },
    };
}

export default async function PostPage({ params }: { params: Promise<{ slug: string }> }) {
    const { slug } = await params;
    const page = blog.getPage([slug]);

    if (!page) {
        notFound();
    }

    return (
        <>
            <ReadProgress />

            <JsonLd data={page.data} slug={slug} />

            <div className="wrap">
                <div className="art-top">
                    <Link className="back-link" href="/">
                        <span className="arr">←</span> Back to blog
                    </Link>
                </div>

                <ArticleHeader data={page.data} />

                <ArticleTOC page={page} slug={slug} />
            </div>
        </>
    );
}
