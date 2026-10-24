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

import { ESuccessionKind, ESupportStatus, type ITrackedProject } from "@/models";
import { dependencyEcosystemLabelKey, dependencyHealthColor, supportSignalLabelKey } from "@/utils";

import { HealthStatusBadge } from "../../HealthStatusBadge";

import { TrackedProjectDetail } from "./TrackedProjectDetail";
import styles from "./styles.module.css";

interface IProps {
    trackedProject: ITrackedProject;
}

export const TrackedProjectRow = ({ trackedProject }: IProps) => {
    const { t } = useTranslation();

    const [open, setOpen] = useState<boolean>(false);

    const { softwareProject, serviceCount, share } = trackedProject;
    const { status, succession } = softwareProject;
    const risky = status !== ESupportStatus.ACTIVE;

    return (
        <>
            <div className={`${styles.MainWrapper} ${open ? styles.Open : ""}`}>
                <div className={styles.Row} onClick={() => setOpen((prev) => !prev)}>
                    <span className={`TextUltraSmall ${styles.Caret}`}>{open ? "▴" : "▾"}</span>
                    <span className={`TextSmall ${styles.Project}`}>{softwareProject.displayName}</span>
                    <span>
                        {risky && <HealthStatusBadge status={status} label={t(supportSignalLabelKey[status])} />}
                    </span>
                    <span className="TextUltraSmall">
                        {risky &&
                            (succession?.kind === ESuccessionKind.SUPERSEDED_BY ? (
                                <span className={styles.Successor}>{t("Dashboard.Dependencies.successor")}</span>
                            ) : (
                                <span className={styles.Muted}>{t("Dashboard.Dependencies.none")}</span>
                            ))}
                    </span>
                    <span className={`TextUltraSmall ${styles.Category}`}>
                        {t(dependencyEcosystemLabelKey[softwareProject.ecosystem])}
                    </span>
                    <span className={`TextSmall ${styles.Services}`}>{serviceCount}</span>
                    <span className={styles.ShareCell}>
                        <span className={styles.ShareTrack}>
                            <span
                                className={styles.ShareBar}
                                style={{
                                    width: `${share}%`,
                                    backgroundColor: risky ? dependencyHealthColor[status] : undefined,
                                }}
                            />
                        </span>
                        <span className={`TextUltraSmall ${styles.ShareValue}`}>{Math.round(share)}%</span>
                    </span>
                </div>

                {open && <TrackedProjectDetail softwareProject={softwareProject} />}
            </div>
        </>
    );
};
