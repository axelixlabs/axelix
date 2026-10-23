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
import { SectionLabel } from "../../SectionLabel";
import shared from "../shared.module.css";

import styles from "./styles.module.css";

const STEPS = [
    {
        title: "Read the Contributing guide",
        description: "Project setup, branching model and PR checklist.",
        href: `${GITHUB_URL}/blob/master/CONTRIBUTING.adoc`,
    },
    {
        title: "Pick an issue ideal for contribution",
        description: "Small tasks the maintainers marked for newcomers.",
        href: `${GITHUB_URL}/labels/ideal-for-contribution`,
    },
    {
        title: "Open a pull request",
        description: "The core team reviews every contribution and helps you land it.",
        href: `${GITHUB_URL}/pulls`,
    },
];

export const Contribute = () => {
    return (
        <section className={`${shared.Section} ${styles.MainWrapper}`} id="contribute">
            <div className={`MainContainer ${shared.TwoColumns}`}>
                <div className={shared.Heading}>
                    <SectionLabel index="04" label="Contribute" dark />
                    <h2 className={`${shared.Title} ${styles.Title}`}>
                        Your first pull request is closer than you think.
                    </h2>
                    <p className={`${shared.Lead} ${styles.Description}`}>
                        Contributions from outside the core team are welcome, from a typo fix to a new feature.
                        Everything merged into the core stays open source under LGPL-3.0.
                    </p>
                </div>
                <ol className={styles.Steps}>
                    {STEPS.map(({ title, description, href }, index) => (
                        <li key={title}>
                            <a href={href} target="_blank" rel="noopener noreferrer" className={styles.Step}>
                                <span className={styles.StepIndex}>{String(index + 1).padStart(2, "0")}</span>
                                <span className={styles.StepText}>
                                    <span className={styles.StepTitle}>{title}</span>
                                    <span className={styles.StepDescription}>{description}</span>
                                </span>
                                <span className={styles.Arrow}>→</span>
                            </a>
                        </li>
                    ))}
                </ol>
            </div>
        </section>
    );
};
