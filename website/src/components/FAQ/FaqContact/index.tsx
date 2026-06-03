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
import styles from "./styles.module.css";

export const FaqFooter = () => {
    return (
        <div className={styles.MainWrapper}>
            <div>
                <span className={styles.Label}>Still curious?</span>
                <h3 className={styles.Title}>Reach the team — we reply within answer working day.</h3>
                <p className={styles.Description}>
                    Architectural questions, production-readiness checks, enterprise pilots. Anything not answered above
                    is the kind of thing we like answering directly.
                </p>
            </div>
            <a href="mailto:hello@axelix.io" className={styles.Email}>
                hello@axelix.io <span className={styles.Arrow}>→</span>
            </a>
        </div>
    );
};
