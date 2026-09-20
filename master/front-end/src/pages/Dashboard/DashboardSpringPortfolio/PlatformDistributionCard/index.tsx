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

import type { IPlatformDistribution } from "@/models";

import { buildLineColorMap, platformFullName, platformShortName } from "../helpers";

import styles from "./styles.module.css";

interface IProps {
    distribution: IPlatformDistribution;
}

const RADIUS = 52;
const CIRCUMFERENCE = 2 * Math.PI * RADIUS;
const LINE_FALLBACK_COLOR = "var(--axelix-bold-grey)";

export const PlatformDistributionCard = ({ distribution }: IProps) => {
    const { t } = useTranslation();
    const colors = buildLineColorMap(distribution);
    const total = distribution.applicationsTotal;
    const fullName = platformFullName(distribution.platform);

    // Contiguous donut arcs, one per line, in major -> line order (newest first).
    let cumulativeOffset = 0;
    const arcs = distribution.majors.flatMap((major) =>
        major.lines.map((line) => {
            const fraction = total > 0 ? line.applicationCount / total : 0;
            const length = fraction * CIRCUMFERENCE;
            const arc = {
                line: line.line,
                color: colors.get(line.line) ?? LINE_FALLBACK_COLOR,
                dashArray: `${length} ${CIRCUMFERENCE - length}`,
                dashOffset: -cumulativeOffset,
            };
            cumulativeOffset += length;
            return arc;
        }),
    );

    const topMajor = distribution.majors[0];

    return (
        <div className={styles.Card}>
            <div className={styles.Head}>
                <span className={styles.Kicker}>{t("Dashboard.SpringPortfolio.generation")}</span>
                <span className={styles.TitleRow}>
                    <span className={styles.Title}>{fullName}</span>
                    <span className={styles.TitleHint}>
                        {t("Dashboard.SpringPortfolio.onSupportedLine", {
                            supported: distribution.applicationsOnOssSupportedLine,
                            total,
                        })}
                    </span>
                </span>
            </div>

            <div className={styles.Body}>
                <span className={styles.DonutWrapper}>
                    <svg width="150" height="150" viewBox="0 0 140 140" className={styles.Donut}>
                        <circle
                            cx="70"
                            cy="70"
                            r={RADIUS}
                            fill="none"
                            stroke="var(--axelix-light-grey)"
                            strokeWidth="22"
                        />
                        {arcs.map((arc) => (
                            <circle
                                key={arc.line}
                                cx="70"
                                cy="70"
                                r={RADIUS}
                                fill="none"
                                stroke={arc.color}
                                strokeWidth="22"
                                strokeDasharray={arc.dashArray}
                                strokeDashoffset={arc.dashOffset}
                            />
                        ))}
                    </svg>
                    <span className={styles.DonutCenter}>
                        <span className={styles.DonutPercent}>
                            {topMajor ? `${topMajor.applicationPercentage}%` : "0%"}
                        </span>
                        {topMajor && (
                            <span
                                className={styles.DonutCaption}
                            >{`${platformShortName(distribution.platform)} ${topMajor.major}`}</span>
                        )}
                    </span>
                </span>

                <div className={styles.Legend}>
                    {distribution.majors.map((major) => (
                        <div key={major.major} className={styles.MajorGroup}>
                            <div className={styles.MajorHead}>
                                <span className={styles.MajorName}>{`${fullName} ${major.major}`}</span>
                                <span className={styles.MajorMeta}>
                                    <span className={styles.MajorCount}>{major.applicationCount}</span>
                                    {` · ${major.applicationPercentage}%`}
                                </span>
                            </div>
                            {major.lines.map((line) => (
                                <div key={line.line} className={styles.LineRow}>
                                    <span className={styles.LineName}>
                                        <span
                                            className={styles.Dot}
                                            style={{ backgroundColor: colors.get(line.line) ?? LINE_FALLBACK_COLOR }}
                                        />
                                        <span className={styles.LineLabel}>{line.line}</span>
                                    </span>
                                    <span className={styles.LineShare}>{line.applicationPercentage}%</span>
                                    <span
                                        className={`${styles.OssChip} ${line.ossSupported ? styles.OssChipOk : styles.OssChipNo}`}
                                    >
                                        {line.ossSupported
                                            ? t("Dashboard.SpringPortfolio.ossOk")
                                            : t("Dashboard.SpringPortfolio.ossNo")}
                                    </span>
                                </div>
                            ))}
                        </div>
                    ))}
                </div>
            </div>

            <div className={styles.Footer}>{t("Dashboard.SpringPortfolio.detectedAtRuntime", { total })}</div>
        </div>
    );
};
