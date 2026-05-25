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

import type { IEnvProperty } from "models";

import styles from "./styles.module.css";

import { CrownIcon } from "assets";

interface IProps {
    /**
     * Single property
     */
    property: IEnvProperty;
}

export const EnvironmentPropertyValue = ({ property }: IProps) => {
    const { value, isPrimary } = property;

    const { t } = useTranslation();

    return (
        <>
            <div className={styles.MainWrapper}>
                {value}
                <Tooltip title={t("Environments.primaryProperty")}>
                    <CrownIcon className={`${styles.PrimaryIcon} ${!isPrimary ? styles.IconPlaceholder : ""}`} />
                </Tooltip>
            </div>
        </>
    );
};
