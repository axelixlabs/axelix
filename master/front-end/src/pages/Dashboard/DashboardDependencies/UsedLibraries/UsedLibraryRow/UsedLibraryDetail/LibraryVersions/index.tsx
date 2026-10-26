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
import { useState } from "react";
import { useTranslation } from "react-i18next";

import type { IUsedLibrary } from "@/models";
import { DEPENDENCY_VERSIONS_SHOWN, DEPENDENCY_VERSION_COLORS } from "@/utils";

import styles from "./styles.module.css";

interface IProps {
    usedLibrary: IUsedLibrary;
}

const percentageOf = (count: number, total: number) => Math.round((count * 100) / total);

export const LibraryVersions = ({ usedLibrary }: IProps) => {
    const { t } = useTranslation();

    const [allVersions, setAllVersions] = useState<boolean>(false);

    const { versions, applicationCount } = usedLibrary;

    const shownVersions = allVersions ? versions : versions.slice(0, DEPENDENCY_VERSIONS_SHOWN);
    const olderVersions = versions.slice(DEPENDENCY_VERSIONS_SHOWN);
    const mostUsedVersion = Math.max(...versions.map((version) => version.applicationCount));

    return (
        <>
            <div className={`TextSmall ${styles.MainWrapper}`}>
                {shownVersions.map(({ version, applicationCount: versionApplications }, index) => (
                    <div key={version} className={styles.Row}>
                        <span className={styles.Version}>{version}</span>
                        <span className={styles.Track}>
                            <span
                                className={styles.Bar}
                                style={{
                                    width: `${(versionApplications * 100) / mostUsedVersion}%`,
                                    backgroundColor:
                                        DEPENDENCY_VERSION_COLORS[index] ?? DEPENDENCY_VERSION_COLORS.at(-1),
                                }}
                            />
                        </span>
                        <span className={styles.Count}>{versionApplications}</span>
                        <span className={`TextUltraSmall ${styles.Share}`}>
                            {percentageOf(versionApplications, applicationCount)}%
                        </span>
                    </div>
                ))}

                {olderVersions.length > 0 && (
                    <button type="button" className={styles.Fold} onClick={() => setAllVersions((prev) => !prev)}>
                        {allVersions
                            ? t("Dashboard.Dependencies.detail.showFewer")
                            : t("Dashboard.Dependencies.detail.olderVersions", {
                                  count: olderVersions.length,
                                  applications: olderVersions.reduce(
                                      (sum, version) => sum + version.applicationCount,
                                      0,
                                  ),
                              })}
                    </button>
                )}
            </div>
        </>
    );
};
