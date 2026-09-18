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

import { type IFrameworkSupportWindow } from "@/models";
import { DEPENDENCY_DATE_FORMAT } from "@/utils";

import styles from "./styles.module.css";

interface IProps {
    framework: IFrameworkSupportWindow;
    outOfOssMaintenance: boolean;
}

export const FrameworkWindowLegend = ({ framework, outOfOssMaintenance }: IProps) => {
    const { t } = useTranslation();

    return (
        <>
            <div className={`TextUltraSmall ${styles.Legend}`}>
                <span className={styles.LegendItem}>
                    <span className={`${styles.LegendDot} ${styles.OssDot}`} />
                    {t("DependenciesAnalyzer.framework.released", {
                        date: dayjs(framework.line.releasedAt).format(DEPENDENCY_DATE_FORMAT),
                    })}
                </span>

                <span className={styles.LegendItem}>
                    <span className={`${styles.LegendDot} ${styles.OssDot}`} />
                    {t(
                        outOfOssMaintenance
                            ? "DependenciesAnalyzer.framework.ossEnded"
                            : "DependenciesAnalyzer.framework.ossEnds",
                        {
                            date: dayjs(framework.line.ossSupportEndsAt).format(DEPENDENCY_DATE_FORMAT),
                        },
                    )}
                </span>

                {framework.line.commercialSupportEndsAt && (
                    <span className={styles.LegendItem}>
                        <span className={`${styles.LegendDot} ${styles.CommercialDot}`} />
                        {t("DependenciesAnalyzer.framework.commercial", {
                            date: dayjs(framework.line.commercialSupportEndsAt).format(DEPENDENCY_DATE_FORMAT),
                        })}
                    </span>
                )}
            </div>
        </>
    );
};
