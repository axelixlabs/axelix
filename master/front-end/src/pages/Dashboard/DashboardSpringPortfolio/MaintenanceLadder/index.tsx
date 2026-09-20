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
import { Fragment } from "react";
import { useTranslation } from "react-i18next";

import type { IMaintenanceWindowEntry, IPlatformName } from "@/models";

import { formatMonthYear, parseIsoDate, platformFullName } from "../helpers";

import styles from "./styles.module.css";

interface IProps {
    entries: IMaintenanceWindowEntry[];
}

const PLATFORM_ORDER: IPlatformName[] = ["SPRING_BOOT", "SPRING_FRAMEWORK"];

const lineToTuple = (line: string): number[] => {
    return line.split(".").map((part) => Number.parseInt(part, 10) || 0);
};

const compareLinesDescending = (a: string, b: string): number => {
    const [aMajor, aMinor] = lineToTuple(a);
    const [bMajor, bMinor] = lineToTuple(b);
    return bMajor - aMajor || bMinor - aMinor;
};

export const MaintenanceLadder = ({ entries }: IProps) => {
    const { t } = useTranslation();

    // Time axis domain: whole calendar years spanning every release and end date, plus today.
    const times = entries.flatMap((entry) => [
        parseIsoDate(entry.releasedAt).getTime(),
        parseIsoDate(entry.ossSupportEndsAt).getTime(),
    ]);
    const today = new Date();
    const startYear = new Date(Math.min(...times, today.getTime())).getFullYear();
    const endYear = new Date(Math.max(...times, today.getTime())).getFullYear() + 1;

    const domainStart = new Date(startYear, 0, 1).getTime();
    const domainEnd = new Date(endYear, 0, 1).getTime();
    const span = domainEnd - domainStart;

    const position = (time: number): number => ((time - domainStart) / span) * 100;

    const years = Array.from({ length: endYear - startYear }, (_, index) => startYear + index);
    const todayPosition = position(today.getTime());

    const groups = PLATFORM_ORDER.map((platform) => ({
        platform,
        rows: entries
            .filter((entry) => entry.platform === platform)
            .sort((a, b) => compareLinesDescending(a.line, b.line)),
    })).filter((group) => group.rows.length > 0);

    return (
        <div className={styles.Wrapper}>
            <div className={styles.SectionHead}>
                <span className={styles.SectionTitle}>{t("Dashboard.SpringPortfolio.ladderTitle")}</span>
                <span className={styles.SectionHint}>{t("Dashboard.SpringPortfolio.ladderHint")}</span>
            </div>

            <div className={styles.Card}>
                <div className={styles.HeaderRow}>
                    <span className={styles.HeaderCell}>{t("Dashboard.SpringPortfolio.line")}</span>
                    <span className={styles.Axis}>
                        {years.map((year) => (
                            <span
                                key={year}
                                className={styles.AxisTick}
                                style={{ left: `${position(new Date(year, 0, 1).getTime())}%` }}
                            >
                                {year}
                            </span>
                        ))}
                    </span>
                    <span className={`${styles.HeaderCell} ${styles.HeaderRight}`}>
                        {t("Dashboard.SpringPortfolio.ossEnds")}
                    </span>
                    <span className={`${styles.HeaderCell} ${styles.HeaderRight}`}>
                        {t("Dashboard.SpringPortfolio.apps")}
                    </span>
                </div>

                <div className={styles.Rows}>
                    <span
                        className={styles.TodayLine}
                        style={{
                            left: `calc(214px + 16px + (100% - 214px - 16px - 104px - 74px - 32px) * ${todayPosition / 100})`,
                        }}
                    />
                    {groups.map((group, groupIndex) => (
                        <Fragment key={group.platform}>
                            {groupIndex > 0 && <div className={styles.GroupGap} />}
                            {group.rows.map((entry) => {
                                const releasedPosition = position(parseIsoDate(entry.releasedAt).getTime());
                                const endPosition = position(parseIsoDate(entry.ossSupportEndsAt).getTime());
                                return (
                                    <div key={`${group.platform}-${entry.line}`} className={styles.Row}>
                                        <span className={styles.LineCell}>
                                            <span
                                                className={`${styles.Dot} ${entry.ossSupported ? styles.DotOk : styles.DotNo}`}
                                            />
                                            <span className={styles.LineLabel}>
                                                {`${platformFullName(entry.platform)} ${entry.line}`}
                                            </span>
                                        </span>
                                        <span className={styles.Track}>
                                            <span
                                                className={styles.Supported}
                                                style={{
                                                    left: `${releasedPosition}%`,
                                                    width: `${Math.max(endPosition - releasedPosition, 0)}%`,
                                                }}
                                            />
                                            <span className={styles.Unsupported} style={{ left: `${endPosition}%` }} />
                                        </span>
                                        <span
                                            className={`${styles.EndsCell} ${entry.ossSupported ? styles.EndsOk : styles.EndsNo}`}
                                        >
                                            {formatMonthYear(entry.ossSupportEndsAt)}
                                        </span>
                                        <span
                                            className={`${styles.AppsCell} ${entry.ossSupported ? styles.AppsOk : styles.AppsNo}`}
                                        >
                                            {entry.applicationCount}
                                        </span>
                                    </div>
                                );
                            })}
                        </Fragment>
                    ))}
                </div>

                <div className={styles.LegendRow}>
                    <span className={styles.LegendItem}>
                        <span className={styles.LegendSupported} />
                        {t("Dashboard.SpringPortfolio.legendSupported")}
                    </span>
                    <span className={styles.LegendItem}>
                        <span className={styles.LegendUnsupported} />
                        {t("Dashboard.SpringPortfolio.legendUnsupported")}
                    </span>
                </div>
            </div>
        </div>
    );
};
