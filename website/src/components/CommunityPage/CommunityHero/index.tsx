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
import Image from "next/image";

import { GITHUB_URL } from "../../../utils";
import { SectionLabel } from "../../SectionLabel";
import { GITHUB_ICON_PATH } from "../icons";
import shared from "../shared.module.css";

import styles from "./styles.module.css";

export const CommunityHero = () => {
    return (
        <section className={styles.MainWrapper}>
            <Image
                src="/community/community-talk.jpg"
                alt="Axelix CEO presenting Axelix at a conference"
                fill
                priority
                sizes="100vw"
                className={styles.Photo}
            />
            <div className={`MainContainer ${styles.Inner}`}>
                <div className={styles.Content}>
                    <SectionLabel label="Community" dark />
                    <h1 className={styles.Title}>
                        Built in the open, <span className={styles.Accent}>with you</span>.
                    </h1>
                    <p className={styles.Description}>
                        Join the engineers running Axelix in production. Ask questions, shape the roadmap and ship
                        features with the core team.
                    </p>
                    <div className={styles.Actions}>
                        <a href={GITHUB_URL} target="_blank" rel="noopener noreferrer" className={styles.PrimaryButton}>
                            <svg width="18" height="18" viewBox="0 0 24 24" className={shared.Icon} aria-hidden>
                                <path d={GITHUB_ICON_PATH} />
                            </svg>
                            Star on GitHub
                        </a>
                        <a href="#contribute" className={styles.SecondaryButton}>
                            Start contributing →
                        </a>
                    </div>
                </div>
            </div>
            <p className={styles.Caption}>
                <b>Mikhail Polivakha</b> · CEO, presenting Axelix
            </p>
        </section>
    );
};
