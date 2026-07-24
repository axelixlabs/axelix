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

interface IProps {
    /**
     * The total number of analyzed transactions, all of which are clean.
     */
    analyzed: number;
}

export const CleanState = ({ analyzed }: IProps) => {
    const { t } = useTranslation();

    return (
        <div className={styles.CleanState}>
            <span className={styles.Check} />
            <h2 className={styles.Title}>{t("Transactional.clean.title")}</h2>
            <p className={styles.Description}>{t("Transactional.clean.description", { count: analyzed })}</p>
        </div>
    );
};
