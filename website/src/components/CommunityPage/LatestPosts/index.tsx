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
import { BLOG_URL } from "../../../utils";
import { SectionLabel } from "../../SectionLabel";
import shared from "../shared.module.css";

import styles from "./styles.module.css";

const POSTS = [
    {
        slug: "environment-post-processor",
        title: "Spring Boot's Hidden Secret for Configuration: EnvironmentPostProcessor",
        date: "Sep 28, 2026",
    },
    { slug: "software-versioning", title: "The Tragedy of Software Versioning", date: "Sep 21, 2026" },
    {
        slug: "java-new-features-in-production",
        title: "New Java Features Are Not Just for Interviews",
        date: "Sep 16, 2026",
    },
];

export const LatestPosts = () => {
    return (
        <section className={`${shared.Section} ${shared.PaperSection}`} id="blog">
            <div className={`MainContainer ${styles.Inner}`}>
                <div className={shared.Heading}>
                    <SectionLabel index="03" label="Latest from the blog" />
                    <div className={styles.HeadingRow}>
                        <h2 className={`${shared.Title} ${styles.Title}`}>Deep dives on Java and Spring Boot.</h2>
                        <a href={BLOG_URL} target="_blank" rel="noopener noreferrer" className={shared.TextLink}>
                            All articles →
                        </a>
                    </div>
                </div>
                <div className={styles.Posts}>
                    {POSTS.map(({ slug, title, date }) => (
                        <a
                            key={slug}
                            href={`${BLOG_URL}/${slug}`}
                            target="_blank"
                            rel="noopener noreferrer"
                            className={styles.Post}
                        >
                            <div
                                className={styles.Cover}
                                style={{ backgroundImage: `url(${BLOG_URL}/posts/${slug}/preview.png)` }}
                            />
                            <span className={styles.Meta}>{date} · Mikhail Polivakha</span>
                            <span className={styles.PostTitle}>{title}</span>
                        </a>
                    ))}
                </div>
            </div>
        </section>
    );
};
