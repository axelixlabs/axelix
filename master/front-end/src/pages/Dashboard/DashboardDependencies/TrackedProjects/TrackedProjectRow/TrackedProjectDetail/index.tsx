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

import { LinkIcon } from "@/assets";
import { ESuccessionKind, ESupportStatus, type ISoftwareProject } from "@/models";

import styles from "./styles.module.css";

interface IProps {
    softwareProject: ISoftwareProject;
}

export const TrackedProjectDetail = ({ softwareProject }: IProps) => {
    const { t } = useTranslation();

    const { succession } = softwareProject;
    const supersededBy = succession?.kind === ESuccessionKind.SUPERSEDED_BY;

    return (
        <>
            <div className={`TextSmall ${styles.MainWrapper}`}>
                <span className={`TextUltraSmall ${styles.Label}`}>
                    {t("Dashboard.Dependencies.detail.projectStatus")}
                </span>
                <span>{softwareProject.summary}</span>

                <span className={`TextUltraSmall ${styles.Label}`}>{t("Dashboard.Dependencies.detail.libraries")}</span>
                <span className={styles.Libraries}>
                    {softwareProject.libraries.map(({ groupId, artifactId }) => (
                        <span key={`${groupId}:${artifactId}`}>{`${groupId}:${artifactId}`}</span>
                    ))}
                </span>

                {softwareProject.status !== ESupportStatus.ACTIVE && (
                    <>
                        <span className={`TextUltraSmall ${styles.Label}`}>
                            {t(
                                supersededBy
                                    ? "Dashboard.Dependencies.detail.supersededBy"
                                    : "Dashboard.Dependencies.detail.replacement",
                            )}
                        </span>
                        <span className={supersededBy ? styles.Code : styles.Muted}>
                            {succession?.value ?? t("Dashboard.Dependencies.detail.noSuccessor")}
                        </span>
                    </>
                )}

                <span className={`TextUltraSmall ${styles.Label}`}>{t("Dashboard.Dependencies.detail.reference")}</span>
                <span className={styles.Reference}>
                    <a
                        href={softwareProject.reference.url}
                        target="_blank"
                        rel="noopener noreferrer"
                        className={styles.ReferenceLink}
                    >
                        {softwareProject.reference.label}
                    </a>
                    <LinkIcon className={styles.ReferenceIcon} />
                </span>
            </div>
        </>
    );
};
