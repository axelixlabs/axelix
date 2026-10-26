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
import { percentageOf } from "@/helpers";
import type { IVersionUsage } from "@/models";

import styles from "./styles.module.css";

interface IProps {
    versionUsage: IVersionUsage;

    /**
     * The number of applications on the most used version of the library, the bar of which spans the whole track.
     */
    mostUsedVersion: number;

    /**
     * The number of applications that use the library in any version.
     */
    libraryApplications: number;
    color: string;
}

export const VersionRow = ({ versionUsage, mostUsedVersion, libraryApplications, color }: IProps) => {
    const { version, applicationCount } = versionUsage;

    return (
        <>
            <div className={styles.MainWrapper}>
                <span className={styles.Version}>{version}</span>
                <span className={styles.Track}>
                    <span
                        className={styles.Bar}
                        style={{ width: `${(applicationCount * 100) / mostUsedVersion}%`, backgroundColor: color }}
                    />
                </span>
                <span className={styles.Count}>{applicationCount}</span>
                <span className={`TextUltraSmall ${styles.Share}`}>
                    {percentageOf(applicationCount, libraryApplications)}%
                </span>
            </div>
        </>
    );
};
