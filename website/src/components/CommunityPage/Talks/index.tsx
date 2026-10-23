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

export const Talks = () => {
    return (
        <section className={shared.Section} id="talks">
            <div className={`MainContainer ${shared.TwoColumns}`}>
                <div className={shared.Heading}>
                    <SectionLabel index="05" label="Talks & meetups" />
                    <h2 className={shared.Title}>Invite the Axelix team to your meetup or conference.</h2>
                </div>
                <div className={styles.Body}>
                    <p className={shared.Lead}>
                        The core team takes an active part in the open-source community and is happy to speak at
                        conferences and meetups. We talk about Spring Boot in production, observability and how Axelix
                        is built.
                    </p>
                    <a href={`mailto:${CONTACT_EMAIL}`} className={styles.Button}>
                        {CONTACT_EMAIL} →
                    </a>
                </div>
            </div>
        </section>
    );
};
