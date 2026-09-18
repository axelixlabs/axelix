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

import { EFrameworkSupportStatus, type IFrameworkSupportWindow } from "@/models";
import { frameworkSupportStatusLabelKey } from "@/utils";

import { FrameworkWindowFacts } from "./FrameworkWindowFacts";
import { FrameworkWindowTimeline } from "./FrameworkWindowTimeline";
import styles from "./styles.module.css";

interface IProps {
    framework: IFrameworkSupportWindow;
}

export const FrameworkWindowPanel = ({ framework }: IProps) => {
    const { t } = useTranslation();

    const outOfOssMaintenance = dayjs().isAfter(dayjs(framework.line.ossSupportEndsAt));

    return (
        <>
            <div className={`${styles.Panel} ${outOfOssMaintenance ? styles.Breached : styles.Supported}`}>
                <div className={styles.Window}>
                    <div className={styles.Heading}>
                        <div className={styles.Version}>
                            <span className={`TextUltraSmall ${styles.FrameworkName}`}>{framework.name}</span>
                            <span className="TextLarge">{framework.version}</span>
                            <span className={`TextUltraSmall ${styles.Line}`}>
                                {t("DependenciesAnalyzer.framework.runningLine", {
                                    line: framework.line.line,
                                    latest: framework.latestKnownLine.line,
                                })}
                            </span>
                        </div>
                        <span className={`TextUltraSmall ${styles.StatusBadge}`}>
                            <span className={styles.StatusDot} />
                            {t(
                                frameworkSupportStatusLabelKey[
                                    outOfOssMaintenance
                                        ? EFrameworkSupportStatus.OUT_OF_OSS_MAINTENANCE
                                        : EFrameworkSupportStatus.OSS_SUPPORTED
                                ],
                            )}
                        </span>
                    </div>

                    <FrameworkWindowTimeline framework={framework} outOfOssMaintenance={outOfOssMaintenance} />
                </div>

                <FrameworkWindowFacts framework={framework} outOfOssMaintenance={outOfOssMaintenance} />
            </div>
        </>
    );
};
