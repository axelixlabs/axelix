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

import { ERulerTickAnnotation, type IRulerTick } from "@/models";

import styles from "./styles.module.css";

interface IProps {
    ticks: IRulerTick[];
    droppedServiceCount: number;
}

export const UpgradesRulerLabels = ({ ticks, droppedServiceCount }: IProps) => {
    const { t } = useTranslation();

    return (
        <>
            <div className={styles.MainWrapper}>
                {ticks.map(({ version, annotation, offset, isSupported }) => (
                    <div className={styles.RulerTick} key={version}>
                        <div
                            className={`TextUltraSmall ${styles.TickLabel} ${isSupported ? styles.TickWhiteLabel : ""}`}
                        >
                            {version}
                        </div>

                        {annotation && (
                            <span className={`TextUltraSmall ${styles.TickAnnotation}`}>
                                {annotation === ERulerTickAnnotation.OLDEST_SEEN && t("Upgrades.Ruler.oldestSeen")}
                                {annotation === ERulerTickAnnotation.MASTER_NOW && t("Upgrades.Ruler.masterNow")}
                                {annotation === ERulerTickAnnotation.SAFE_NEXT &&
                                    t("Upgrades.Ruler.safeNext", {
                                        offset: offset,
                                    })}
                                {annotation === ERulerTickAnnotation.DROP &&
                                    t("Upgrades.Ruler.drop", {
                                        offset: offset,
                                        services: droppedServiceCount,
                                    })}
                            </span>
                        )}
                    </div>
                ))}
            </div>
        </>
    );
};
