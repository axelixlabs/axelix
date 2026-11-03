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
import { Button } from "antd";
import { useTranslation } from "react-i18next";

import { DOCS_URL } from "@/utils";

import styles from "./styles.module.css";

interface IProps {
    masterVersion: string;
    compatibilityWindow: number;
}

export const UpgradesEmptyState = ({ masterVersion, compatibilityWindow }: IProps) => {
    const { t } = useTranslation();

    return (
        <>
            <div className={styles.EmptyStateCard}>
                <div className={`TextUltraSmall ${styles.EmptyStateLabel}`}>{t("Upgrades.EmptyState.label")}</div>

                <div className="TextMedium">{t("Upgrades.EmptyState.title", { masterVersion: masterVersion })}</div>

                <div className={`TextSmall ${styles.EmptyStateDescription}`}>
                    {t("Upgrades.EmptyState.description", { count: compatibilityWindow })}
                </div>

                <Button
                    type="primary"
                    target="_blank"
                    rel="noopener noreferrer"
                    href={DOCS_URL}
                    className={styles.ReadDocumentation}
                >
                    {t("Upgrades.EmptyState.readDocumentation")}
                </Button>
            </div>
        </>
    );
};
