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
import Link from "next/link";

import { ENTERPRISE_HREF, GET_STARTED_HREF } from "../../utils";

import styles from "./styles.module.css";

export const EditionsBanner = () => {
    return (
        <section className={styles.MainWrapper}>
            <div className={`MainContainer ${styles.Inner}`}>
                <div className={styles.Text}>
                    <h2 className={styles.Title}>Open source or Enterprise?</h2>
                    <p className={styles.Description}>
                        The open-source core is free under LGPL-3.0. Enterprise is a drop-in distribution that adds
                        control at scale.
                    </p>
                </div>
                <div className={styles.Actions}>
                    <Link href={`${ENTERPRISE_HREF}#editions`} className={styles.PrimaryButton}>
                        Compare editions →
                    </Link>
                    <a href={GET_STARTED_HREF} className={styles.SecondaryButton}>
                        Get started free
                    </a>
                </div>
            </div>
        </section>
    );
};
