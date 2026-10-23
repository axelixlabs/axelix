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
import { ENTERPRISE_EMAIL } from "@/utils";

import { SectionLabel } from "../../SectionLabel";
import shared from "../shared.module.css";

import styles from "./styles.module.css";

const PILOT_STEPS = [
    {
        step: "01",
        title: "Intro call",
        body: "Tell us about your services, your stack and what hurts most in production.",
    },
    {
        step: "02",
        title: "Pilot on your fleet",
        body: "We set up Enterprise with you on a few services and agree on the policies to enforce.",
    },
    {
        step: "03",
        title: "Shape the roadmap",
        body: "Regular check-ins with the core team. Your feedback decides what ships first.",
    },
];

export const PilotCta = () => {
    return (
        <section className={styles.MainWrapper}>
            <div className={`MainContainer ${styles.Inner}`}>
                <div className={styles.Intro}>
                    <SectionLabel index="04" label="Early access" dark />
                    <h2 className={`${shared.Title} ${styles.Title}`}>Want a seat in the first pilots?</h2>
                    <p className={styles.Description}>
                        We&apos;re picking a handful of teams to shape what ships first. Tell us about your fleet and
                        we&apos;ll be in touch within a working day.
                    </p>
                </div>
                <div className={styles.Steps}>
                    <span className={shared.MonoLabel}>How the pilot works</span>
                    <ol>
                        {PILOT_STEPS.map(({ step, title, body }) => (
                            <li key={step} className={styles.Step}>
                                <span className={styles.StepNumber}>{step}</span>
                                <div className={styles.StepText}>
                                    <span className={styles.StepTitle}>{title}</span>
                                    <span className={styles.StepBody}>{body}</span>
                                </div>
                            </li>
                        ))}
                    </ol>
                    <a href={`mailto:${ENTERPRISE_EMAIL}`} className={`${shared.PrimaryButton} ${styles.Button}`}>
                        {ENTERPRISE_EMAIL} →
                    </a>
                </div>
            </div>
        </section>
    );
};
