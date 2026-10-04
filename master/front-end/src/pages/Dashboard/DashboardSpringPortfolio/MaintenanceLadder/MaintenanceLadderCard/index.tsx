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

import { parseIsoDate } from "@/helpers";
import type { IMaintenanceWindowEntry } from "@/models";
import { PLATFORM_ORDER } from "@/utils";

import { MaintenanceLadderCardHeader } from "./MaintenanceLadderCardHeader";
import { MaintenanceLadderCardLegend } from "./MaintenanceLadderCardLegend";
import { MaintenanceLadderCardRow } from "./MaintenanceLadderCardRow";
import styles from "./styles.module.css";

interface IProps {
    entries: IMaintenanceWindowEntry[];
}

const lineToTuple = (line: string): number[] => {
    return line.split(".").map((part) => Number.parseInt(part, 10) || 0);
};

const compareLinesDescending = (a: string, b: string): number => {
    const [aMajor, aMinor] = lineToTuple(a);
    const [bMajor, bMinor] = lineToTuple(b);
    return bMajor - aMajor || bMinor - aMinor;
};

export const MaintenanceLadderCard = ({ entries }: IProps) => {
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

    const todayPosition = position(today.getTime());

    const groups = PLATFORM_ORDER.map((platform) => ({
        platform,
        rows: entries
            .filter((entry) => entry.platform === platform)
            .sort((a, b) => compareLinesDescending(a.line, b.line)),
    })).filter((group) => group.rows.length > 0);

    return (
        <>
            <div className={styles.MainWrapper}>
                <MaintenanceLadderCardHeader startYear={startYear} endYear={endYear} position={position} />

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
                                return (
                                    <MaintenanceLadderCardRow
                                        entry={entry}
                                        position={position}
                                        key={`${group.platform}-${entry.line}`}
                                    />
                                );
                            })}
                        </Fragment>
                    ))}
                </div>

                <MaintenanceLadderCardLegend />
            </div>
        </>
    );
};
