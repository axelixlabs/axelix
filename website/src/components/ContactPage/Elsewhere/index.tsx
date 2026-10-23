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
import { GITHUB_URL } from "../../../utils";
import { GITHUB_ICON_PATH, LINKEDIN_ICON_PATH, X_ICON_PATH } from "../../CommunityPage/icons";
import { SectionLabel } from "../../SectionLabel";
import shared from "../shared.module.css";

import styles from "./styles.module.css";

const SOCIAL = [
    { name: "GitHub", href: GITHUB_URL, icon: GITHUB_ICON_PATH },
    { name: "X", href: "https://x.com/axelixlabs", icon: X_ICON_PATH },
    { name: "LinkedIn", href: "https://www.linkedin.com/company/axelix-labs", icon: LINKEDIN_ICON_PATH },
];

export const Elsewhere = () => {
    return (
        <section className={`${shared.Section} ${shared.PaperSection}`} id="elsewhere">
            <div className={`MainContainer ${shared.TwoColumns} ${styles.Inner}`}>
                <div className={shared.Heading}>
                    <SectionLabel index="03" label="Elsewhere" />
                    <h2 className={shared.Title}>Follow Axelix Labs.</h2>
                </div>
                <div className={styles.Links}>
                    {SOCIAL.map(({ name, href, icon }) => (
                        <a key={name} href={href} target="_blank" rel="noopener noreferrer" className={styles.Link}>
                            <svg width="18" height="18" viewBox="0 0 24 24" className={styles.Icon} aria-hidden>
                                <path d={icon} />
                            </svg>
                            {name}
                        </a>
                    ))}
                </div>
            </div>
        </section>
    );
};
