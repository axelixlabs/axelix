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

export const RiskChip = ({ risk }: IProps) => {
    const { t } = useTranslation();
    const { reason, hint, kind } = risk;

    const kindClass = KIND_STYLES[risk.kind];
    const label = t(`Environments.triageTags.${kind}`);
    const hintLabel =
        kind === EPropertyTriageTag.DANGEROUS ? t("Environments.recommended") : t("Environments.replacedBy");

    return (
        <>
            <HintTooltip
                placement="right"
                content={
                    <div className={`${styles.Tooltip} ${kindClass}`}>
                        <div className={`TextUltraSmall ${styles.TooltipLabel}`}>
                            <div className={styles.TooltipDot} />

                            {label}
                        </div>

                        <div>{reason}</div>

                        {hint && (
                            <div className="TextUltraSmall">
                                <div className={styles.TooltipHintLabel}>{hintLabel}</div>
                                <div className={styles.TooltipHintValue}>{hint}</div>
                            </div>
                        )}
                    </div>
                }
            >
                <span className={`${styles.Chip} ${kindClass}`}>
                    <span className={styles.ChipDot} />
                    {label}
                </span>
            </HintTooltip>
        </>
    );
};
