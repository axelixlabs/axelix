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
import type { IRulerTick } from "@/models";

import styles from "./styles.module.css";

interface IProps {
    ticks: IRulerTick[];
}

export const UpgradesRulerTrack = ({ ticks }: IProps) => {
    const supportedStart = ticks.findIndex(({ isSupported }) => isSupported);
    const supportedCount = ticks.filter(({ isSupported }) => isSupported).length;
    const hasSupportedRange = supportedStart >= 0 && supportedCount > 0 && ticks.length > 0;

    return (
        <div className={styles.Track}>
            {hasSupportedRange && (
                <div
                    className={styles.Supported}
                    style={{
                        left: `${(supportedStart / ticks.length) * 100}%`,
                        width: `${(supportedCount / ticks.length) * 100}%`,
                    }}
                />
            )}
        </div>
    );
};
