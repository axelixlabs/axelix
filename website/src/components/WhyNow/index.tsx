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
import { SectionLabel } from "../SectionLabel";

import styles from "./styles.module.css";

const STATS = [
    {
        label: "01 · Incidents",
        value: "3.4×",
        description:
            "more production incidents per merged PR in teams with high AI adoption. Monthly incidents rose 57.9%.",
        source: "Faros AI Engineering Report 2026 · 22,000 developers",
        sourceHref: "https://www.faros.ai/blog/ai-acceleration-whiplash-takeaways",
        note: "Incidents per PR up 23.5% and change failure rate up about 30% year over year.",
        noteSource: "Cortex Engineering Benchmark 2026",
        noteHref:
            "https://www.testsprite.com/blog/why-change-failure-rates-are-rising-30-and-what-your-team-can-do-about-it",
    },
    {
        label: "02 · Quality",
        value: "1.7×",
        description:
            "more issues in AI-authored pull requests. Performance problems such as excessive I/O appeared nearly 8× more often.",
        source: "CodeRabbit, State of AI vs Human Code, Dec 2025",
        sourceHref: "https://www.coderabbit.ai/blog/state-of-ai-vs-human-code-generation-report",
        note: "AI adoption continues to have a negative relationship with software delivery stability.",
        noteSource: "DORA 2025 · ~5,000 professionals",
        noteHref: "https://cloud.google.com/blog/products/ai-machine-learning/announcing-the-2025-dora-report",
    },
    {
        label: "03 · Vulnerabilities",
        value: "59k",
        description: "CVEs forecast for 2026, the first year above 50,000. 2025 closed at a record 49,972.",
        source: "FIRST Vulnerability Forecast 2026",
        sourceHref: "https://www.infosecurity-magazine.com/news/first-forecasts-record-50000-cve/",
        note: "A growing share of software depends on end-of-life releases that receive no patches, so their vulnerabilities become permanent.",
        noteSource: "Sonatype SSCR 2026",
        noteHref: "https://www.sonatype.com/state-of-the-software-supply-chain/2026/vulnerability-management",
    },
];

const JOBS = [
    {
        index: "02",
        title: "Stop known issues before they ship",
        description:
            "Catch known anti-patterns in your code, configuration, dependencies and JVM setup before they reach production.",
        href: "#quality",
    },
    {
        index: "03",
        title: "Find the cause of incidents fast",
        description: "Look inside the running service with your AI agent and find the root cause without a redeploy.",
        href: "#debug",
    },
];

const EXTERNAL = { target: "_blank", rel: "noopener noreferrer" };

export const WhyNow = () => {
    return (
        <section className={styles.MainWrapper} id="problem">
            <div className="MainContainer">
                <SectionLabel index="01" label="What AI changed" dark />
                <h2 className={styles.Title}>
                    AI ships more code, faster. <span className={styles.Underlined}>Production pays for it.</span>
                </h2>
                <p className={styles.Description}>
                    The 2025–2026 research points the same way: more incidents per change, lower code quality, and a
                    record number of vulnerabilities in the libraries we depend on.
                </p>

                <div className={styles.Stats}>
                    {STATS.map((stat) => (
                        <div key={stat.label} className={styles.Stat}>
                            <div className={styles.StatLabel}>{stat.label}</div>
                            <div className={styles.StatValue}>{stat.value}</div>
                            <p className={styles.StatDescription}>{stat.description}</p>
                            <a href={stat.sourceHref} className={styles.StatSource} {...EXTERNAL}>
                                {stat.source} ↗
                            </a>
                            <div className={styles.StatNoteWrapper}>
                                <div className={styles.StatNote}>
                                    {stat.note}{" "}
                                    <a href={stat.noteHref} className={styles.StatNoteLink} {...EXTERNAL}>
                                        {stat.noteSource} ↗
                                    </a>
                                </div>
                            </div>
                        </div>
                    ))}
                </div>

                <div className={styles.Jobs}>
                    <h3 className={styles.JobsTitle}>
                        Axelix does two jobs, from <span className={styles.Accent}>one install and one console</span>.
                    </h3>
                    <div className={styles.JobCards}>
                        {JOBS.map(({ index, title, description, href }) => (
                            <a key={index} href={href} className={styles.JobCard}>
                                <span className={styles.JobIndex}>{index}</span>
                                <span className={styles.JobTitle}>{title}</span>
                                <span className={styles.JobDescription}>{description}</span>
                                <span className={styles.JobButton}>See how ↓</span>
                            </a>
                        ))}
                    </div>
                </div>
            </div>
        </section>
    );
};
