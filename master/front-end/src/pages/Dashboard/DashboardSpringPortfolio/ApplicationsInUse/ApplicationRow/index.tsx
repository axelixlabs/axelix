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
import type { ISpringPortfolioApplication } from "@/models";

import { OssChip } from "../../OssChip";

import styles from "./styles.module.css";

interface IProps {
    application: ISpringPortfolioApplication;
}

export const ApplicationRow = ({ application }: IProps) => {
    const { name, springBootLine, springFrameworkLine, ossSupported } = application;
    const dotClassName = `${styles.Dot} ${ossSupported ? styles.DotOk : styles.DotNo}`;

    return (
        <>
            <div className={styles.MainWrapper}>
                <span className={`TextSmall ${styles.Name}`}>{name}</span>
                <span className={`TextSmall ${styles.Line}`}>
                    <span className={dotClassName} />
                    {springBootLine}
                </span>
                <span className={`TextSmall ${styles.Line}`}>
                    <span className={dotClassName} />
                    {springFrameworkLine}
                </span>
                <span>
                    <OssChip ossSupported={ossSupported} />
                </span>
            </div>
        </>
    );
};
