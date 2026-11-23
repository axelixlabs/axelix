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

import type { IStarterVersion } from "@/models";
import { DONUT_COLORS } from "@/utils";

import styles from "./styles.module.css";

interface IProps {
    starterVersions: IStarterVersion[];
    total: number;
}

export const StarterVersionBars = ({ starterVersions, total }: IProps) => {
    const { t, i18n } = useTranslation();
    const percent = new Intl.NumberFormat(i18n.resolvedLanguage, { style: "percent", maximumFractionDigits: 1 });

    return (
        <article className={styles.Card}>
            <h2 className={styles.ColumnTitle} id="starter-versions-title">
                {t("Upgrades.FleetBars.title")}
            </h2>
            <p className={`TextUltraSmall ${styles.Meta}`}>
                {t("Upgrades.FleetBars.serviceCount", { count: total })} · {t("Upgrades.FleetBars.observationWindow")}
            </p>
            <ul className={styles.FleetBars} aria-labelledby="starter-versions-title">
                {starterVersions.map(({ version, serviceCount }, index) => {
                    const share = serviceCount / total;

                    return (
                        <li className={styles.FleetBarRow} key={version}>
                            <div className={styles.FleetBarLabel}>
                                <strong className={styles.Version}>{version}</strong>
                                <span className={styles.Meta}>
                                    {t("Upgrades.Ruler.impact", {
                                        services: t("Upgrades.FleetBars.serviceCount", { count: serviceCount }),
                                        percentage: percent.format(share),
                                    })}
                                </span>
                            </div>
                            <div className={styles.FleetBarTrack} aria-hidden="true">
                                <span
                                    style={{
                                        width: `${share * 100}%`,
                                        backgroundColor: DONUT_COLORS[index % DONUT_COLORS.length],
                                    }}
                                />
                            </div>
                        </li>
                    );
                })}
            </ul>
        </article>
    );
};
