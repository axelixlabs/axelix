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
import { CONTACT_EMAIL } from "../../../utils";
import { SectionLabel } from "../../SectionLabel";
import shared from "../shared.module.css";

import styles from "./styles.module.css";

const PARTNERS = [
    {
        name: "CyberJAR",
        logo: "/community/partners/cyberjar.jpg",
        description: "Practical, state-of-the-art Java on YouTube, by BellSoft developer advocates.",
        cta: "Watch on YouTube",
        href: "https://www.youtube.com/channel/UCUCyBfJ6jOiQsAc1jCxGABQ",
    },
    {
        name: "Foojay",
        logo: "/community/partners/foojay.png",
        description: "The vendor-neutral home for the Java community: news, podcast and OpenJDK data.",
        cta: "Visit foojay.io",
        href: "https://foojay.io",
    },
];

export const Partners = () => {
    return (
        <section className={shared.Section} id="partners">
            <div className={`MainContainer ${shared.TwoColumns} ${styles.Inner}`}>
                <div className={shared.Heading}>
                    <SectionLabel index="02" label="Partners" />
                    <h2 className={shared.Title}>Communities we grow with.</h2>
                    <p className={`${shared.Lead} ${styles.Description}`}>
                        We partner with communities that share our love for Java. We promote each other&apos;s work and
                        take part in each other&apos;s events.
                    </p>
                    <a href={`mailto:${CONTACT_EMAIL}`} className={shared.TextLink}>
                        Want to partner with us? {CONTACT_EMAIL} →
                    </a>
                </div>
                <div className={styles.Cards}>
                    {PARTNERS.map(({ name, logo, description, cta, href }) => (
                        <a key={name} href={href} target="_blank" rel="noopener noreferrer" className={styles.Card}>
                            <div
                                role="img"
                                aria-label={`${name} logo`}
                                className={styles.Logo}
                                style={{ backgroundImage: `url(${logo})` }}
                            />
                            <span className={styles.CardDescription}>{description}</span>
                            <span className={styles.CardCta}>{cta} ↗</span>
                        </a>
                    ))}
                </div>
            </div>
        </section>
    );
};
