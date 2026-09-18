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
import dayjs from "dayjs";
import { useTranslation } from "react-i18next";

import { elapsedMonthsSince } from "@/helpers";
import type { IFrameworkSupportWindow } from "@/models";
import { EFrameworkSupportStatus } from "@/models";
import { DEPENDENCY_MONTH_FORMAT, frameworkSupportConsequenceKey } from "@/utils";

import styles from "./styles.module.css";

interface IProps {
    framework: IFrameworkSupportWindow;
    outOfOssMaintenance: boolean;
}

export const FrameworkWindowFacts = ({ framework, outOfOssMaintenance }: IProps) => {
    const { t } = useTranslation();

    const elapsedMonths = elapsedMonthsSince(framework.line.ossSupportEndsAt);
    const years = Math.floor(elapsedMonths / 12);
    const months = elapsedMonths % 12;

    const elapsed =
        [
            years > 0 ? t("DependenciesAnalyzer.duration.years", { count: years }) : "",
            months > 0 ? t("DependenciesAnalyzer.duration.months", { count: months }) : "",
        ]
            .filter(Boolean)
            .join(" ") || t("DependenciesAnalyzer.duration.lessThanAMonth");

    return (
        <>
            <div className={`TextUltraSmall ${styles.MainWrapper}`}>
                <span className={styles.FactLabel}>{t("DependenciesAnalyzer.framework.consequence")}</span>
                <span className={`TextSmall ${styles.FactProse}`}>
                    {t(
                        frameworkSupportConsequenceKey[
                            outOfOssMaintenance
                                ? EFrameworkSupportStatus.OUT_OF_OSS_MAINTENANCE
                                : EFrameworkSupportStatus.OSS_SUPPORTED
                        ],
                    )}
                </span>
                {outOfOssMaintenance && (
                    <>
                        <span className={styles.FactLabel}>{t("DependenciesAnalyzer.framework.outOfOssFor")}</span>
                        <span className={`${styles.FactValue} ${styles.Breaching}`}>{elapsed}</span>
                    </>
                )}
                <span className={styles.FactLabel}>{t("DependenciesAnalyzer.framework.supportedTarget")}</span>
                <span className={styles.FactValue}>
                    {t("DependenciesAnalyzer.framework.targetValue", {
                        line: framework.oldestSupportedLine.line,
                        date: dayjs(framework.oldestSupportedLine.ossSupportEndsAt).format(DEPENDENCY_MONTH_FORMAT),
                    })}
                </span>
            </div>
        </>
    );
};
