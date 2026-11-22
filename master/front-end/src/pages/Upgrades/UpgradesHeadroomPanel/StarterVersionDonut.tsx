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

import { DistributionDonut } from "@/components/DistributionDonut";
import type { IStarterVersion } from "@/models";
import { DONUT_COLORS } from "@/utils";

import styles from "./styles.module.css";

interface IProps {
    starterVersions: IStarterVersion[];
    total: number;
}

export const StarterVersionDonut = ({ starterVersions, total }: IProps) => {
    const { t, i18n } = useTranslation();
    const number = new Intl.NumberFormat(i18n.resolvedLanguage);
    const percent = new Intl.NumberFormat(i18n.resolvedLanguage, { style: "percent", maximumFractionDigits: 1 });
    const segments = starterVersions.map(({ version, serviceCount }, index) => ({
        version,
        serviceCount,
        value: (serviceCount / total) * 100,
        color: DONUT_COLORS[index % DONUT_COLORS.length],
    }));

    return (
        <article className={styles.Card}>
            <h2 className={styles.ColumnTitle} id="starter-versions-title">
                {t("Upgrades.FleetBars.title")}
            </h2>
            <p className={`TextUltraSmall ${styles.Meta}`}>{t("Upgrades.FleetBars.observationWindow")}</p>
            <div className={styles.FleetChart}>
                <DistributionDonut
                    segments={segments}
                    centreTop={number.format(total)}
                    centreBottom={t("Upgrades.FleetBars.serviceLabel", { count: total })}
                />

                <div className={`${styles.FleetLegend} DonutLegendFade`}>
                    <table aria-labelledby="starter-versions-title">
                        <thead>
                            <tr>
                                <th scope="col">{t("Upgrades.ArtifactsTable.starter")}</th>
                                <th scope="col">{t("Upgrades.FleetBars.serviceLabel", { count: total })}</th>
                                <th scope="col">{t("Dashboard.Dependencies.share")}</th>
                            </tr>
                        </thead>
                        <tbody>
                            {segments.map(({ version, serviceCount, value, color }) => (
                                <tr key={version}>
                                    <th scope="row">
                                        <span className={styles.Version}>
                                            <span
                                                aria-hidden="true"
                                                className={styles.Swatch}
                                                style={{ backgroundColor: color }}
                                            />
                                            {version}
                                        </span>
                                    </th>
                                    <td>{number.format(serviceCount)}</td>
                                    <td>{percent.format(value / 100)}</td>
                                </tr>
                            ))}
                        </tbody>
                    </table>
                </div>
            </div>
        </article>
    );
};
