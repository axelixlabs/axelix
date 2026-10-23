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
import { GITHUB_URL } from "@/utils";

import { SectionLabel } from "../../SectionLabel";
import shared from "../shared.module.css";

import styles from "./styles.module.css";

const COMMITMENTS = [
    {
        label: "License",
        title: "LGPL-3.0, an OSI-approved license",
        body: "The GNU Lesser General Public License, version 3. A recognized open-source license your legal team already knows.",
    },
    {
        label: "Commercial use",
        title: "Free in closed-source software",
        body: "Use Axelix OSS in proprietary, commercial products at no cost. There is no requirement to publish your own source code anywhere.",
    },
    {
        label: "Contributions",
        title: "Changes to the core stay open",
        body: "Every contribution to the open-source core remains open source, under the same license, for everyone.",
    },
];

const DROP_IN_STEPS = [
    {
        title: "Swap the distribution",
        body: "Replace the Axelix OSS dependency with Axelix Enterprise.",
    },
    {
        title: "Keep your setup",
        body: "Your configuration, console and MCP integrations carry over unchanged.",
    },
    {
        title: "Turn on what you need",
        body: "Enable policies, SSO, RBAC and audit when your team is ready.",
    },
];

export const OpenCore = () => {
    return (
        <section className={styles.MainWrapper} id="open-core">
            <div className="MainContainer">
                <div className={styles.Inner}>
                    <div>
                        <SectionLabel index="01" label="The open core" />
                        <h2 className={shared.Title}>The core is open source, and it stays that way.</h2>
                        <p className={styles.Description}>
                            Axelix OSS is distributed under the GNU Lesser General Public License, version 3, an
                            OSI-approved open-source license. You can use it free of charge, including in closed-source
                            commercial software, and the open-source edition is never cut down to make room for
                            Enterprise.
                        </p>
                        <a href={GITHUB_URL} target="_blank" rel="noopener noreferrer" className={styles.SourceLink}>
                            View the source on GitHub →
                        </a>
                    </div>
                    <div className={styles.Commitments}>
                        {COMMITMENTS.map(({ label, title, body }) => (
                            <div key={label} className={styles.Commitment}>
                                <span className={`${shared.MonoLabel} ${styles.CommitmentLabel}`}>{label}</span>
                                <div className={styles.CommitmentText}>
                                    <span className={styles.CommitmentTitle}>{title}</span>
                                    <span className={styles.CommitmentBody}>{body}</span>
                                </div>
                            </div>
                        ))}
                    </div>
                </div>

                <div className={styles.DropIn}>
                    <div className={styles.DropInIntro}>
                        <span className={`${shared.MonoLabel} ${styles.DropInLabel}`}>Drop-in replacement</span>
                        <h3 className={styles.DropInTitle}>
                            Enterprise is a full distribution of Axelix, not an add-on.
                        </h3>
                        <p className={styles.DropInDescription}>
                            Axelix Enterprise replaces the open-source edition in place. It contains everything in OSS,
                            so moving from one to the other is a smooth swap, not a migration project.
                        </p>
                    </div>
                    <ol>
                        {DROP_IN_STEPS.map(({ title, body }, index) => (
                            <li key={title} className={styles.Step}>
                                <span className={styles.StepNumber}>{String(index + 1).padStart(2, "0")}</span>
                                <div className={styles.StepText}>
                                    <span className={styles.StepTitle}>{title}</span>
                                    <span className={styles.StepBody}>{body}</span>
                                </div>
                            </li>
                        ))}
                    </ol>
                </div>
            </div>
        </section>
    );
};
