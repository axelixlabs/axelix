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

import { SectionLabel } from "../../SectionLabel";
import shared from "../shared.module.css";

import styles from "./styles.module.css";

export const People = () => {
    return (
        <section className={styles.MainWrapper} id="people">
            <div className={`MainContainer ${shared.TwoColumns} ${styles.Inner}`}>
                <div className={shared.Heading}>
                    <SectionLabel index="02" label="Who we are" dark />
                    <h2 className={`${shared.Title} ${styles.Title}`}>
                        Built by people who contribute to Spring itself.
                    </h2>
                    <p className={`${shared.Lead} ${styles.Description}`}>
                        Our CEO is a recognized Spring Data contributor and has presented Axelix at Java conferences
                        such as JPoint. The rest of the team contributes to Spring modules too, so the problems Axelix
                        finds are ones we&apos;ve debugged ourselves.
                    </p>
                </div>
                <figure className={styles.Figure}>
                    <div className={styles.Photo}>
                        <Image
                            src="/about/ceo-jpoint.jpg"
                            alt="Axelix CEO demoing Axelix on stage at JPoint"
                            fill
                            sizes="(width <= 980px) 100vw, 620px"
                            className={styles.PhotoImage}
                        />
                    </div>
                    <figcaption className={styles.Caption}>
                        <span>Demoing Axelix live at JPoint.</span>
                        <span>
                            <b>Mikhail Polivakha</b> · CEO
                        </span>
                    </figcaption>
                </figure>
            </div>
        </section>
    );
};
