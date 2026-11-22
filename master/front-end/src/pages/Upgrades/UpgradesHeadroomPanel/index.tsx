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

import { getOldestStarterVersion, getTotalServiceCount, getUnsupportedServiceCount } from "@/helpers";
import type { IUpgradesData } from "@/models";

import { StarterVersionDonut } from "./StarterVersionDonut";
import styles from "./styles.module.css";

interface IProps {
    data: IUpgradesData;
}

export const UpgradesHeadroomPanel = ({ data }: IProps) => {
    const { t, i18n } = useTranslation();
    const { masterVersion, starterVersions, compatibilityWindow } = data;
    const oldestStarter = getOldestStarterVersion(starterVersions);
    const total = getTotalServiceCount(starterVersions);
    const [major, minor] = masterVersion.split(".").map(Number);
    const currentVersion = `${major}.${minor}`;
    const currentImpact = getUnsupportedServiceCount(currentVersion, starterVersions, compatibilityWindow);
    const percent = new Intl.NumberFormat(i18n.resolvedLanguage, { style: "percent", maximumFractionDigits: 1 });

    const futureTargets = Array.from({ length: Math.max(3, compatibilityWindow) }, (_, index) => {
        const version = `${major}.${minor + index + 1}`;
        const count = getUnsupportedServiceCount(version, starterVersions, compatibilityWindow);

        return {
            version,
            isSupported: count === 0,
            impact: t("Upgrades.Ruler.impact", {
                services: t("Upgrades.FleetBars.serviceCount", { count }),
                percentage: percent.format(count / total),
            }),
        };
    });
    const safeIndex = futureTargets.findLastIndex(({ isSupported }) => isSupported);
    const safeTarget = futureTargets[safeIndex];
    // Show three targets by default, extending the ruler through the first release beyond safe headroom.
    const targets = futureTargets.slice(0, Math.max(3, safeIndex + 2));

    return (
        <section className={styles.Panel}>
            <article className={`${styles.Card} ${styles.Headroom}`}>
                <h2 className={`TextCaps ${styles.Eyebrow}`}>{t("Upgrades.HeadroomPanel.eyebrow")}</h2>
                <p className={`TextMedium ${styles.Title}`}>
                    {safeTarget
                        ? t("Upgrades.HeadroomPanel.safeTarget", { version: safeTarget.version })
                        : t("Upgrades.HeadroomPanel.noSafeTarget")}
                </p>
                <p className={`TextSmall ${styles.Meta}`}>
                    {t("Upgrades.HeadroomPanel.oldestVersion", { version: oldestStarter })}
                </p>

                <div className={styles.Ruler}>
                    <div className={styles.Tick} data-supported={currentImpact === 0}>
                        <strong title={masterVersion}>{currentVersion}</strong>
                        <span>{t("Upgrades.Ruler.masterNow")}</span>
                    </div>
                    {targets.map(({ version, isSupported, impact }) => (
                        <div className={styles.Tick} key={version} data-supported={isSupported}>
                            <strong>{version}</strong>
                            <span>{impact}</span>
                        </div>
                    ))}
                </div>
                <p className={`TextUltraSmall ${styles.Meta}`}>{t("Upgrades.Ruler.potentialTargets")}</p>
            </article>

            <article className={styles.Card}>
                <h2 className={styles.ColumnTitle}>{t("Upgrades.HeadroomPanel.impact")}</h2>
                <table className={styles.ImpactTable}>
                    <thead>
                        <tr>
                            <th scope="col">{t("Upgrades.HeadroomPanel.targetVersion")}</th>
                            <th scope="col">{t("Upgrades.HeadroomPanel.outsideSupport")}</th>
                        </tr>
                    </thead>
                    <tbody>
                        {targets.map(({ version, impact }) => (
                            <tr key={version}>
                                <th scope="row">{version}</th>
                                <td>{impact}</td>
                            </tr>
                        ))}
                    </tbody>
                </table>
            </article>

            <StarterVersionDonut starterVersions={starterVersions} total={total} />
        </section>
    );
};
