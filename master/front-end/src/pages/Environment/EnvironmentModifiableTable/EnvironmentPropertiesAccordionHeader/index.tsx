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

import { InfoIcon } from "@/assets";
import { HintTooltip } from "@/components";
import type { IEnvProperty, IEnvironmentPropertySource } from "@/models";

import styles from "./styles.module.css";

interface IProps {
    properties: IEnvProperty[];
    propertySource: IEnvironmentPropertySource;
}

export const EnvironmentPropertiesAccordionHeader = ({ properties, propertySource }: IProps) => {
    const { t } = useTranslation();

    const { name, description } = propertySource;

    const deprecatedCount = properties.filter(({ deprecation }) => deprecation).length;

    return (
        <>
            <div className={styles.MainWrapper}>
                <span className={styles.TitleWrapper}>
                    <span className={styles.Title}>{name}</span>
                    {description && (
                        <HintTooltip
                            placement="bottomLeft"
                            content={
                                <>
                                    <span className={styles.TooltipName}>{name}</span>
                                    <span>{description}</span>
                                </>
                            }
                        >
                            <span className={styles.InfoTrigger}>
                                <InfoIcon color="currentColor" />
                            </span>
                        </HintTooltip>
                    )}
                </span>

                <span className={styles.Counters}>
                    {deprecatedCount > 0 && (
                        <span className={styles.FlaggedCounter}>
                            {t("Environments.flaggedCount", { value: deprecatedCount })}
                        </span>
                    )}
                    <span className={styles.PropertiesCounter}>
                        {t("Environments.propertiesCount", { value: properties.length })}
                    </span>
                </span>
            </div>
        </>
    );
};
