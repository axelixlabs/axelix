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

import type { ILicenseFunction } from "models";

import styles from "./styles.module.css";
import { GoldCrownIcon } from "assets";

export interface IProps {
    capabilities: ILicenseFunction[];
}

export const LicenseCapabilities = ({ capabilities }: IProps) => {
    const { t } = useTranslation();

    const sortedCapabilities = capabilities.toSorted(
        (prevCapability, nextCapability) => Number(nextCapability.enabled) - Number(prevCapability.enabled),
    );

    return (
        <>
            <div className="TextSmall">
                <div className={styles.CapabilitiesTitle}>{t("LicenseModal.included")}</div>

                <div className={styles.CapabilityChipsWrapper}>
                    {sortedCapabilities.map(({ name, enabled }) => {
                        const isDisabled = !enabled;

                        return (
                            <span
                                className={styles.CapabilityChip}
                            >
                                {name} {isDisabled && <GoldCrownIcon />}
                            </span>
                        );
                    })}
                </div>
            </div>
        </>
    );
};
