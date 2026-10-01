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
import { Copy } from "@/components";
import { type TPropertyRiskKind, propertyRisks } from "@/helpers";
import { EPropertyTriageTag, type IEnvProperty } from "@/models";

import { EnvironmentPropertyValue } from "../../EnvironmentPropertyValue";

import { RiskChip } from "./RiskChip";
import styles from "./styles.module.css";

interface IProps {
    /**
     * Single property
     */
    property: IEnvProperty;
}

const RISK_ROW_STYLES: Record<TPropertyRiskKind, string> = {
    [EPropertyTriageTag.DANGEROUS]: styles.DangerousRow,
    [EPropertyTriageTag.DEPRECATED_ERROR]: styles.DeprecatedErrorRow,
    [EPropertyTriageTag.DEPRECATED_WARNING]: styles.DeprecatedWarningRow,
};

export const EnvironmentProperty = ({ property }: IProps) => {
    const { name, isPrimary } = property;

    const risks = propertyRisks(property);
    const primaryRisk = risks[0];

    const rowStyles = [
        styles.MainWrapper,
        primaryRisk ? RISK_ROW_STYLES[primaryRisk.kind] : "",
        !primaryRisk && !isPrimary ? styles.SuppressedRow : "",
    ]
        .filter(Boolean)
        .join(" ");

    return (
        <>
            <div className={rowStyles}>
                <span className={styles.CaretCell} />

                <div className={styles.KeyChunk}>
                    <span className={styles.Key}>{name}</span>
                    <Copy text={name} />
                    {risks.map((risk) => (
                        <RiskChip risk={risk} key={risk.kind} />
                    ))}
                </div>

                <EnvironmentPropertyValue property={property} />
            </div>
        </>
    );
};
