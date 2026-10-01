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

import { HintTooltip } from "@/components";
import type { IPropertyRisk, TPropertyRiskKind } from "@/helpers";
import { EPropertyTriageTag } from "@/models";

import styles from "./styles.module.css";

interface IProps {
    /**
     * The risk to render, as classified by {@link propertyRisks}
     */
    risk: IPropertyRisk;
}

const KIND_STYLES: Record<TPropertyRiskKind, string> = {
    [EPropertyTriageTag.DANGEROUS]: styles.Dangerous,
    [EPropertyTriageTag.DEPRECATED_ERROR]: styles.DeprecatedError,
    [EPropertyTriageTag.DEPRECATED_WARNING]: styles.DeprecatedWarning,
};

/**
 * An inline pill next to a property key that names a risk and, on hover, explains why it is flagged
 * and what to do about it. The reasoning lives in the tooltip so it never lengthens the row.
 */
export const RiskChip = ({ risk }: IProps) => {
    const { t } = useTranslation();

    const kindClass = KIND_STYLES[risk.kind];
    const label = t(`Environments.triageTags.${risk.kind}`);
    const hintLabel =
        risk.kind === EPropertyTriageTag.DANGEROUS ? t("Environments.recommended") : t("Environments.replacedBy");

    return (
        <HintTooltip
            placement="right"
            content={
                <span className={`${styles.Tooltip} ${kindClass}`}>
                    <span className={styles.TooltipLabel}>
                        <span className={styles.TooltipDot} />
                        {label}
                    </span>
                    <span>{risk.reason}</span>
                    {risk.hint && (
                        <span className={styles.TooltipHint}>
                            <span className={styles.TooltipHintLabel}>{hintLabel}</span>
                            <span className={styles.TooltipHintValue}>{risk.hint}</span>
                        </span>
                    )}
                </span>
            }
        >
            <span className={`${styles.Chip} ${kindClass}`}>
                <span className={styles.ChipDot} />
                {label}
            </span>
        </HintTooltip>
    );
};
