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
import { Input } from "antd";
import { useState } from "react";

import styles from "./styles.module.css";

interface IProps {
    masterVersion: string;
}

export const UpgradeBundleVersionInput = ({ masterVersion }: IProps) => {
    const [bundleVersion, setBundleVersion] = useState<string>("");

    return (
        <>
            <div className={`TextUltraSmall ${styles.MainWrapper}`}>
                <span className={styles.Label}>Holding a Master bundle? Check the version you actually have: </span>
                <Input
                    size="small"
                    value={bundleVersion}
                    onChange={(e) => setBundleVersion(e.target.value)}
                    placeholder={masterVersion}
                    className={styles.Input}
                />

                <span className={styles.BundleInputHint}>Safe - all 57 services stay supported</span>
            </div>

            <div className={`TextUltraSmall ${styles.Note}`}>
                Going two ahead is possible, but not free: on a 1.6 build the 5 services on starter 1.2 would fall out
                of the support window and stop reporting data. They would remain listed here, marked unsupported. The
                upgrade itself is never blocked.
            </div>
        </>
    );
};
