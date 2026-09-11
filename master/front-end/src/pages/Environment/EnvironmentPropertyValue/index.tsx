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
import { Tooltip } from "antd";
import { useTranslation } from "react-i18next";

import type { IEnvProperty } from "@/models";

import styles from "./styles.module.css";

interface IProps {
    /**
     * Single property
     */
    property: IEnvProperty;
}

/*
 * Both marks are inlined rather than added to the shared icon set: they exist only to tint with the
 * badge they sit in, which `currentColor` on a local node gives for free.
 */
const ActiveMark = () => (
    <svg width="11" height="11" viewBox="0 0 16 16" fill="none" stroke="currentColor" strokeWidth="2.2">
        <path d="M3 8.6 6.2 11.8 13 5" strokeLinecap="round" strokeLinejoin="round" />
    </svg>
);

const SuppressedMark = () => (
    <svg width="11" height="11" viewBox="0 0 16 16" fill="none" stroke="currentColor" strokeWidth="1.8">
        <path d="M2.4 8s2.2-4 5.6-4 5.6 4 5.6 4-2.2 4-5.6 4S2.4 8 2.4 8z" strokeLinecap="round" />
        <path d="M3 13 13 3" strokeLinecap="round" />
    </svg>
);

export const EnvironmentPropertyValue = ({ property }: IProps) => {
    const { value, isPrimary } = property;

    const { t } = useTranslation();

    const statusKey = isPrimary ? "primaryProperty" : "suppressedProperty";

    return (
        <>
            <span className={`${styles.Value} ${!isPrimary ? styles.SuppressedValue : ""}`} title={value}>
                {value}
            </span>

            <span className={styles.StatusCell}>
                <Tooltip title={t(`Environments.${statusKey}Hint`)}>
                    <span className={`${styles.Status} ${isPrimary ? styles.Active : styles.Suppressed}`}>
                        {isPrimary ? <ActiveMark /> : <SuppressedMark />}
                        {t(`Environments.${statusKey}`)}
                    </span>
                </Tooltip>
            </span>
        </>
    );
};
