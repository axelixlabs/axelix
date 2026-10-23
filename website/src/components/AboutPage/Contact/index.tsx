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
import { CONTACT_EMAIL, ENTERPRISE_EMAIL, GITHUB_ISSUES_URL, getLinkTargetProps } from "@/utils";

import { SectionLabel } from "../../SectionLabel";
import shared from "../shared.module.css";

import styles from "./styles.module.css";

const CHANNELS = [
    { label: "General", value: CONTACT_EMAIL, href: `mailto:${CONTACT_EMAIL}`, external: false },
    { label: "Enterprise & pilots", value: ENTERPRISE_EMAIL, href: `mailto:${ENTERPRISE_EMAIL}`, external: false },
    { label: "Bugs & feature requests", value: "GitHub issues", href: GITHUB_ISSUES_URL, external: true },
];

export const Contact = () => {
    return (
        <section className={shared.Section} id="contact">
            <div className={`MainContainer ${shared.TwoColumns}`}>
                <div className={shared.Heading}>
                    <SectionLabel index="05" label="Contact" />
                    <h2 className={shared.Title}>We&apos;d love to hear from you.</h2>
                    <p className={`${shared.Lead} ${styles.Description}`}>
                        A question, a bug, an idea, or just a story about a bad night on call. A real person on the team
                        reads every message.
                    </p>
                </div>
                <div className={styles.Channels}>
                    {CHANNELS.map(({ label, value, href, external }) => (
                        <a key={label} href={href} className={styles.Channel} {...getLinkTargetProps(external)}>
                            <span className={styles.ChannelText}>
                                <span className={styles.ChannelLabel}>{label}</span>
                                <span className={styles.ChannelValue}>{value}</span>
                            </span>
                            <span className={styles.Arrow}>{external ? "↗" : "→"}</span>
                        </a>
                    ))}
                </div>
            </div>
        </section>
    );
};
