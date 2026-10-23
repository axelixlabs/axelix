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
import { LINKEDIN_ICON_PATH, RSS_ICON_PATH, X_ICON_PATH } from "../icons";
import shared from "../shared.module.css";

import styles from "./styles.module.css";

const CHANNELS = [
    {
        name: "X",
        description: "Release announcements and short updates",
        href: "https://x.com/axelixlabs",
        icon: X_ICON_PATH,
    },
    {
        name: "LinkedIn",
        description: "Company news and case studies",
        href: "https://www.linkedin.com/company/axelix-labs",
        icon: LINKEDIN_ICON_PATH,
    },
    {
        name: "Axelix Blog",
        description: "Deep dives on Java and Spring Boot",
        href: BLOG_URL,
        icon: RSS_ICON_PATH,
    },
];

export const FollowAlong = () => {
    return (
        <section className={`${shared.Section} ${shared.PaperSection}`} id="follow">
            <div className={`MainContainer ${shared.TwoColumns}`}>
                <div className={shared.Heading}>
                    <SectionLabel index="01" label="Follow along" />
                    <h2 className={shared.Title}>Updates, demos and articles, wherever you read.</h2>
                </div>
                <div className={styles.List}>
                    {CHANNELS.map(({ name, description, href, icon }) => (
                        <a key={name} href={href} target="_blank" rel="noopener noreferrer" className={styles.Row}>
                            <svg width="22" height="22" viewBox="0 0 24 24" className={shared.Icon} aria-hidden>
                                <path d={icon} />
                            </svg>
                            <span className={styles.Name}>{name}</span>
                            <span className={styles.Description}>{description}</span>
                            <span className={styles.Arrow}>↗</span>
                        </a>
                    ))}
                </div>
            </div>
        </section>
    );
};
