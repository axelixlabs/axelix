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
import { MOTIVATION_DOCS_URL } from "@/utils";

import { SectionLabel } from "../../SectionLabel";
import shared from "../shared.module.css";

import styles from "./styles.module.css";

const STATS = [
    {
        value: "3.4×",
        description: "more production incidents per merged PR in teams with high AI adoption",
        source: "Faros AI, 2026",
        href: "https://www.faros.ai/blog/ai-acceleration-whiplash-takeaways",
    },
    {
        value: "1.7×",
        description: "more issues in AI-authored pull requests than in human-written ones",
        source: "CodeRabbit, Dec 2025",
        href: "https://www.coderabbit.ai/blog/state-of-ai-vs-human-code-generation-report",
    },
    {
        value: "59k",
        description: "CVEs forecast for 2026, after a record 49,972 in 2025",
        source: "FIRST, 2026",
        href: "https://www.infosecurity-magazine.com/news/first-forecasts-record-50000-cve/",
    },
];

export const Mission = () => {
    return (
        <section className={`${shared.Section} ${shared.PaperSection}`} id="mission">
            <div className={`MainContainer ${styles.Inner}`}>
                <div className={shared.TwoColumns}>
                    <div className={shared.Heading}>
                        <SectionLabel index="01" label="Why we build it" />
                        <h2 className={shared.Title}>Axelix is our response to what AI did to production.</h2>
                    </div>
                    <div className={`${shared.Lead} ${styles.Text}`}>
                        <p>
                            As AI writes more of the code we ship, software gets less stable. Production sees more
                            incidents, code quality drops and more bugs slip through, and the number of discovered CVEs
                            keeps setting records.
                        </p>
                        <p>
                            The Java ecosystem is no exception. We built Axelix to help Java teams handle these new
                            challenges: find the cause of an incident fast, keep weak code out of production, and stay
                            on supported dependencies.
                        </p>
                    </div>
                </div>

                <div className={styles.Stats}>
                    {STATS.map(({ value, description, source, href }) => (
                        <div key={value} className={styles.Stat}>
                            <span className={styles.StatValue}>{value}</span>
                            <span className={styles.StatDescription}>{description}</span>
                            <a href={href} target="_blank" rel="noopener noreferrer" className={styles.StatSource}>
                                {source} ↗
                            </a>
                        </div>
                    ))}
                </div>

                <a href={MOTIVATION_DOCS_URL} target="_blank" rel="noopener noreferrer" className={styles.StoryLink}>
                    Read the full story: Why we built Axelix →
                </a>
            </div>
        </section>
    );
};
