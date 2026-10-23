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
import { SectionLabel } from "../../SectionLabel";
import shared from "../shared.module.css";

import styles from "./styles.module.css";

const PRINCIPLES = [
    {
        title: "Committed to open source",
        body: "The Axelix core is licensed under LGPL-3.0 and will always stay open. We believe that is how trust is built, so Enterprise builds on top of the core and never forks it.",
    },
    {
        title: "Embracing the AI revolution",
        body: "AI agents are already part of how teams work. Axelix is built to integrate well with them, so they can take on more and make engineers even more productive.",
    },
    {
        title: "AI empowers developers, not replaces them",
        body: "AI makes engineers faster, but people stay in charge. That is why policies matter: they keep what reaches production under your control, whoever wrote the code.",
    },
];

export const Principles = () => {
    return (
        <section className={shared.Section} id="principles">
            <div className={`MainContainer ${styles.Inner}`}>
                <div className={shared.Heading}>
                    <SectionLabel index="03" label="How we build" />
                    <h2 className={`${shared.Title} ${styles.Title}`}>A few things we hold ourselves to.</h2>
                </div>
                <ol className={styles.Cards}>
                    {PRINCIPLES.map(({ title, body }, index) => (
                        <li key={title} className={styles.Card}>
                            <span className={styles.Number}>{String(index + 1).padStart(2, "0")}</span>
                            <span className={styles.CardTitle}>{title}</span>
                            <span className={styles.CardBody}>{body}</span>
                        </li>
                    ))}
                </ol>
            </div>
        </section>
    );
};
