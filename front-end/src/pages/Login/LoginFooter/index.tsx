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
import { useTranslation } from "react-i18next";

import styles from "./styles.module.css";

export const LoginFooter = () => {
    const { t } = useTranslation();

    return (
        <>
            <div className={`TextUltraSmall ${styles.MainWrapper}`}>
                <a
                    href="https://axelix.io/docs/introduction"
                    target="_blank"
                    rel="noopener noreferrer"
                    className={styles.Link}
                >
                    {t("Authentication.docs")}
                </a>
                <div className={styles.Divider}>|</div>
                <a href="https://axelix.io/blog" target="_blank" rel="noopener noreferrer" className={styles.Link}>
                    {t("blog")}
                </a>
                <div className={styles.Divider}>|</div>
                <div className={styles.Version}>v1.0.0</div>
            </div>
        </>
    );
};
