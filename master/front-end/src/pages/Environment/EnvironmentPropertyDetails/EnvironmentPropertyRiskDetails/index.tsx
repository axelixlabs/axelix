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

import type { IDangerousValue, IDeprecation } from "@/models";

import { EnvironmentPropertyDetailRow } from "../EnvironmentPropertyDetailRow";

import styles from "./styles.module.css";

interface IProps {
    /**
     * The deprecation details of the property, if any
     */
    deprecation?: IDeprecation;

    /**
     * The dangerous-value details of the property, if any
     */
    dangerousValue?: IDangerousValue;
}

/**
 * The risk-related rows of the expanded property view: why a property is deprecated or dangerous and,
 * where the metadata gives one, the property to migrate to or a safer value to use.
 */
export const EnvironmentPropertyRiskDetails = ({ deprecation, dangerousValue }: IProps) => {
    const { t } = useTranslation();

    return (
        <>
            {deprecation && (
                <EnvironmentPropertyDetailRow label={t("Environments.deprecated")}>
                    {deprecation.message}
                </EnvironmentPropertyDetailRow>
            )}

            {deprecation?.replacedBy && (
                <EnvironmentPropertyDetailRow label={t("Environments.replacedBy")}>
                    <span className={styles.PropertyName}>{deprecation.replacedBy}</span>
                </EnvironmentPropertyDetailRow>
            )}

            {dangerousValue && (
                <EnvironmentPropertyDetailRow label={t("Environments.dangerousValue")}>
                    {dangerousValue.rationale}
                </EnvironmentPropertyDetailRow>
            )}

            {dangerousValue?.alternativeExample && (
                <EnvironmentPropertyDetailRow label={t("Environments.recommended")}>
                    <span className={styles.PropertyName}>{dangerousValue.alternativeExample}</span>
                </EnvironmentPropertyDetailRow>
            )}
        </>
    );
};
