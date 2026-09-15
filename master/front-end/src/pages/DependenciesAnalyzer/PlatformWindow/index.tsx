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

import { buildSupportTimeline, elapsedMonthsSince } from "@/helpers";
import { EPlatformSupportStatus, type IPlatformSupportWindow } from "@/models";
import {
    DEPENDENCY_DATE_FORMAT,
    DEPENDENCY_MONTH_FORMAT,
    platformSupportConsequenceKey,
    platformSupportStatusLabelKey,
} from "@/utils";

import styles from "./styles.module.css";

interface IProps {
    /**
     * The maintenance window of the platform the instance runs on.
     */
    platform: IPlatformSupportWindow;

    /**
     * The ISO timestamp the analysis was taken at.
     */
    analyzedAt: string;
}

export const PlatformWindow = ({ platform, analyzedAt }: IProps) => {
    const { t } = useTranslation();

    const timeline = buildSupportTimeline(platform);
    const outOfOssMaintenance = platform.status === EPlatformSupportStatus.OUT_OF_OSS_MAINTENANCE;

    const elapsedMonths = elapsedMonthsSince(platform.ossSupportEndsAt);
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
            <div className={styles.MainWrapper}>
                <div className={styles.Caption}>
                    <span className={`TextUltraSmall ${styles.CaptionLabel}`}>
                        {t("DependenciesAnalyzer.platform.caption", { platform: platform.name })}
                    </span>
                    <span className={`TextUltraSmall ${styles.CaptionSource}`}>
                        {t("DependenciesAnalyzer.platform.source", {
                            date: dayjs(analyzedAt).format(DEPENDENCY_DATE_FORMAT),
                        })}
                    </span>
                </div>

                <div className={`${styles.Panel} ${outOfOssMaintenance ? styles.Breached : styles.Supported}`}>
                    <div className={styles.Window}>
                        <div className={styles.Heading}>
                            <div className={styles.Version}>
                                <span className={`TextUltraSmall ${styles.PlatformName}`}>{platform.name}</span>
                                <span className={styles.VersionValue}>{platform.version}</span>
                                <span className={`TextUltraSmall ${styles.Line}`}>
                                    {t("DependenciesAnalyzer.platform.runningLine", {
                                        line: platform.line,
                                        latest: platform.latestKnownLine,
                                    })}
                                </span>
                            </div>
                            <span className={`TextUltraSmall ${styles.StatusBadge}`}>
                                <span className={styles.StatusDot} />
                                {t(platformSupportStatusLabelKey[platform.status])}
                            </span>
                        </div>

                        <div className={styles.Timeline}>
                            <div className={styles.Bar}>
                                <span className={styles.OssSegment} style={{ width: `${timeline.ossPercentage}%` }} />
                                <span
                                    className={styles.CommercialSegment}
                                    style={{ width: `${timeline.commercialPercentage}%` }}
                                />
                            </div>
                            <div className={styles.TodayTrack}>
                                <span className={styles.TodayMark} style={{ left: `${timeline.todayPercentage}%` }} />
                                <span className={styles.TodayLabel} style={{ left: `${timeline.todayPercentage}%` }}>
                                    {t("DependenciesAnalyzer.platform.today")}
                                </span>
                            </div>
                            <div className={`TextUltraSmall ${styles.Legend}`}>
                                <span className={styles.LegendItem}>
                                    <span className={`${styles.LegendDot} ${styles.OssDot}`} />
                                    {t("DependenciesAnalyzer.platform.released", {
                                        date: dayjs(platform.releasedAt).format(DEPENDENCY_DATE_FORMAT),
                                    })}
                                </span>
                                <span className={styles.LegendItem}>
                                    <span className={`${styles.LegendDot} ${styles.OssDot}`} />
                                    {t(
                                        outOfOssMaintenance
                                            ? "DependenciesAnalyzer.platform.ossEnded"
                                            : "DependenciesAnalyzer.platform.ossEnds",
                                        { date: dayjs(platform.ossSupportEndsAt).format(DEPENDENCY_DATE_FORMAT) },
                                    )}
                                </span>
                                {platform.commercialSupportEndsAt && (
                                    <span className={styles.LegendItem}>
                                        <span className={`${styles.LegendDot} ${styles.CommercialDot}`} />
                                        {t("DependenciesAnalyzer.platform.commercial", {
                                            date: dayjs(platform.commercialSupportEndsAt).format(
                                                DEPENDENCY_DATE_FORMAT,
                                            ),
                                        })}
                                    </span>
                                )}
                            </div>
                        </div>
                    </div>

                    <div className={styles.Facts}>
                        <span className={styles.FactLabel}>{t("DependenciesAnalyzer.platform.consequence")}</span>
                        <span className={`TextSmall ${styles.FactProse}`}>
                            {t(platformSupportConsequenceKey[platform.status])}
                        </span>
                        {outOfOssMaintenance && (
                            <>
                                <span className={styles.FactLabel}>
                                    {t("DependenciesAnalyzer.platform.outOfOssFor")}
                                </span>
                                <span className={`${styles.FactValue} ${styles.Breaching}`}>{elapsed}</span>
                            </>
                        )}
                        <span className={styles.FactLabel}>{t("DependenciesAnalyzer.platform.supportedTarget")}</span>
                        <span className={styles.FactValue}>
                            {t("DependenciesAnalyzer.platform.targetValue", {
                                line: platform.supportedTargetLine,
                                date: dayjs(platform.supportedTargetOssEndsAt).format(DEPENDENCY_MONTH_FORMAT),
                            })}
                        </span>
                    </div>
                </div>
            </div>
        </>
    );
};
