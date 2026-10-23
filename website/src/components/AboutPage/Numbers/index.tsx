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
import { GithubIcon } from "@/assets";
import { GITHUB_URL } from "@/utils";

import { SectionLabel } from "../../SectionLabel";
import shared from "../shared.module.css";

import styles from "./styles.module.css";

const NUMBERS = [
    { value: "200+", description: "companies worldwide using Axelix, open source or Enterprise" },
    { value: "10K+", description: "monthly downloads from Maven Central" },
];

export const Numbers = () => {
    return (
        <section className={`${shared.Section} ${shared.PaperSection} ${styles.MainWrapper}`} id="numbers">
            <div className={`MainContainer ${shared.TwoColumns} ${styles.Inner}`}>
                <div className={shared.Heading}>
                    <SectionLabel index="04" label="Axelix today" />
                    <h2 className={shared.Title}>Already running in production around the world.</h2>
                    <p className={`${shared.Lead} ${styles.Description}`}>
                        Teams add Axelix to their Spring Boot services every day, on the open-source core and on
                        Enterprise. Thank you to everyone who tried it early, filed an issue or told a colleague.
                    </p>
                    <a href={GITHUB_URL} target="_blank" rel="noopener noreferrer" className={styles.StarLink}>
                        <GithubIcon width="14" height="14" />
                        Star on GitHub
                    </a>
                </div>
                <div className={styles.NumbersColumn}>
                    <div className={styles.Cards}>
                        {NUMBERS.map(({ value, description }) => (
                            <div key={value} className={styles.Card}>
                                <span className={styles.Value}>{value}</span>
                                <span className={styles.CardDescription}>{description}</span>
                            </div>
                        ))}
                    </div>
                    <span className={styles.Note}>Estimates based on Maven Central and Scarf data.</span>
                </div>
            </div>
        </section>
    );
};
