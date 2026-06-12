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
import { BlogHomeClient } from "@/components";
import { BLOG_HOME_DESCRIPTION } from "@/lib/blog-metadata";
import { getSortedCardItems } from "@/lib/source";

import { Suspense } from "react";

import styles from "./page.module.css";

// Fully static: we never read `searchParams` here (that would force dynamic
// rendering with `Cache-Control: private`). Filtering/pagination happen on the
// client from the URL; the whole dataset ships in the RSC payload.
export const revalidate = false;

export default async function HomePage() {
    const items = await getSortedCardItems();

    return (
        <>
            <header className={styles.BlogHero}>
                <div className="wrap">
                    <h1>
                        Axelix <span className={styles.Accent}>Blog</span>
                    </h1>
                    <p className={styles.Lede}>{BLOG_HOME_DESCRIPTION}</p>
                </div>
            </header>
            <Suspense>
                <BlogHomeClient items={items} />
            </Suspense>
        </>
    );
}
