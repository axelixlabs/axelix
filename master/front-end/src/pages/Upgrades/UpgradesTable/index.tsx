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
import type { TFunction } from "i18next";
import { useTranslation } from "react-i18next";

import type { ICeilingBlocker } from "@/models";

import styles from "./styles.module.css";

interface IProps {
    ceilingBlockers: ICeilingBlocker[];
}

const formatLastSeen = (lastSeen: string, t: TFunction): string => {
    const seen = dayjs(lastSeen);

    if (!seen.isValid()) {
        return lastSeen;
    }

    const daysAgo = dayjs().startOf("day").diff(seen.startOf("day"), "day");

    if (daysAgo <= 0) {
        return t("Upgrades.ArtifactsTable.today");
    }

    if (daysAgo === 1) {
        return t("Upgrades.ArtifactsTable.yesterday");
    }

    return t("Upgrades.ArtifactsTable.daysAgo", { count: daysAgo });
};

export const UpgradesTable = ({ ceilingBlockers }: IProps) => {
    const { t } = useTranslation();

    return (
        <section className={styles.Section}>
            <div className={styles.SectionRow}>
                <div className={styles.SectionTitle}>{t("Upgrades.ArtifactsTable.sectionTitle")}</div>
                <p className={styles.SectionNote}>{t("Upgrades.ArtifactsTable.sectionNote")}</p>
            </div>

            <div className={styles.Table}>
                <div className={`TextCaps ${styles.HeaderRow}`}>
                    <span>{t("Upgrades.ArtifactsTable.artifact")}</span>
                    <span>{t("Upgrades.ArtifactsTable.starter")}</span>
                    <span>{t("Upgrades.ArtifactsTable.lastSeen")}</span>
                </div>

                {ceilingBlockers.map(({ artifactId, groupId, starterVersion, lastSeen }) => (
                    <div className={styles.Row} key={`${groupId}:${artifactId}`}>
                        <div className={styles.ArtifactCell}>
                            <span className={styles.ArtifactId}>{artifactId}</span>
                            <span className={styles.GroupId}>{groupId}</span>
                        </div>
                        <span className={styles.VersionBadge}>{starterVersion}</span>
                        <span className={styles.LastSeen}>{formatLastSeen(lastSeen, t)}</span>
                    </div>
                ))}
            </div>

            <p className={`TextUltraSmall ${styles.FootNote}`}>{t("Upgrades.ArtifactsTable.footNote")}</p>
        </section>
    );
};
