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

import { HintTooltip } from "@/components";
import { getOldestStarterVersion, getTotalServiceCount, getUnsupportedServiceCount } from "@/helpers";
import type { IUpgradesData } from "@/models";

import { StarterVersionBars } from "./StarterVersionBars";
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
    const number = new Intl.NumberFormat(i18n.resolvedLanguage);
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
    const safeTargets = futureTargets.filter(({ isSupported }) => isSupported);
    const safeTarget = safeTargets.at(-1);
    const firstCompatibleMinor = Math.max(0, minor - compatibilityWindow + 1);
    const countsByVersion = new Map(starterVersions.map(({ version, serviceCount }) => [version, serviceCount]));
    const windowVersions = Array.from({ length: minor - firstCompatibleMinor + 1 }, (_, index) => {
        const version = `${major}.${firstCompatibleMinor + index}`;

        return { version, count: countsByVersion.get(version) ?? 0 };
    });

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

                <div
                    className={styles.Ruler}
                    style={{
                        gridTemplateColumns: [windowVersions.length, safeTargets.length]
                            .filter(Boolean)
                            .map((length) => `minmax(${length * 52}px, ${length}fr)`)
                            .join(" "),
                    }}
                >
                    <div className={styles.RulerRange}>
                        <div className={styles.RangeLabel}>
                            {t("Upgrades.CompatibilityWindow.title")}
                            <HintTooltip
                                content={t("Upgrades.CompatibilityWindow.explanation", {
                                    master: currentVersion,
                                    oldest: `${major}.${firstCompatibleMinor}`,
                                })}
                            >
                                <span className={styles.Question}>?</span>
                            </HintTooltip>
                        </div>
                        <ol className={styles.Ticks}>
                            {windowVersions.map(({ version, count }) => (
                                <li className={styles.Tick} key={version} data-current={version === currentVersion}>
                                    <strong>{version === currentVersion ? masterVersion : version}</strong>
                                    <span title={t("Upgrades.FleetBars.serviceCount", { count })}>
                                        {t("Upgrades.Ruler.impact", {
                                            services: number.format(count),
                                            percentage: percent.format(count / total),
                                        })}
                                    </span>
                                    {version === currentVersion && (
                                        <span className={styles.CurrentLabel}>{t("Upgrades.Ruler.masterNow")}</span>
                                    )}
                                </li>
                            ))}
                        </ol>
                    </div>
                    {safeTargets.length > 0 && (
                        <div className={`${styles.RulerRange} ${styles.SafeRange}`}>
                            <div className={styles.RangeLabel}>{t("Upgrades.Ruler.safeHeadroom")}</div>
                            <ol className={styles.Ticks}>
                                {safeTargets.map(({ version }) => (
                                    <li className={styles.Tick} key={version}>
                                        <strong>{version}</strong>
                                    </li>
                                ))}
                            </ol>
                        </div>
                    )}
                </div>
            </article>

            <StarterVersionBars starterVersions={starterVersions} total={total} />
        </section>
    );
};
