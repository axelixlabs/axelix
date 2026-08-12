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

import { HintTooltip } from "components";
import type { ILicenseFunction } from "models";

import styles from "./styles.module.css";

import { GoldCrownIcon } from "assets";

interface IProps {
    functions: ILicenseFunction[];
}

export const LicenseFunctions = ({ functions }: IProps) => {
    const { t } = useTranslation();

    if (!functions.length) {
        return null;
    }

    const sortedFunctions = functions.toSorted((a, b) => Number(b.enabled) - Number(a.enabled));

    return (
        <>
            <div className="TextSmall">
                <div className={styles.FunctionsTitle}>{t("LicenseModal.included")}</div>

                <div className={styles.FunctionChipsWrapper}>
                    {sortedFunctions.map(({ name, enabled }) => {
                        if (!enabled) {
                            return (
                                <HintTooltip content={t("LicenseModal.Form.availableInEnterprise")} key={name}>
                                    <span className={styles.FunctionChip}>
                                        {name} <GoldCrownIcon />
                                    </span>
                                </HintTooltip>
                            );
                        }

                        return (
                            <span className={styles.FunctionChip} key={name}>
                                {name}
                            </span>
                        );
                    })}
                </div>
            </div>
        </>
    );
};
