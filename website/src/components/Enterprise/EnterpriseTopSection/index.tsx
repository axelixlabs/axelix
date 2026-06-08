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

export const EnterpriseTopSection = () => {
    return (
        <div className={styles.MainWrapper}>
            <div>
                <span className={styles.Eyebrow}>Enterprise · early access</span>
                <h2 className={styles.Title}>
                    Open core stays open. <span className="UnderlinedText">Enterprise lives on top.</span>
                </h2>
            </div>
            <p className={styles.Lead}>
                <em className={`AccentText ${styles.AccentText}`}>Axelix Enterprise</em> extends the open core with the
                controls, integrations and support platform, security and compliance teams ask for — without forking the
                surface engineers already learned. The OSS keeps shipping, in the open. Enterprise is what you reach for
                when one cluster becomes ten and audit asks who changed what at 3 AM.
            </p>
        </div>
    );
};
