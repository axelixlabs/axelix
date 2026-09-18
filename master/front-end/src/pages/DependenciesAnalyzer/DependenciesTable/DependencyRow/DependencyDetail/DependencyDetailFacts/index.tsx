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
import { type IResolvedDependency } from "@/models";
// import { dependencyNoteLabelKey } from "@/utils";

import styles from "./styles.module.css";

interface IProps {
    dependency: IResolvedDependency;
}

export const DependencyDetailFacts = ({ dependency }: IProps) => {
    const { t } = useTranslation();

    const { softwareProject } = dependency;

    if (!softwareProject) {
        return null;
    }

    return (
        <>
            <div className={styles.MainWrapper}>
                <span className={`TextUltraSmall ${styles.Label}`}>
                    {t("DependenciesAnalyzer.detail.projectStatus")}
                </span>
                <span className={`TextSmall ${styles.Prose}`}>{softwareProject.status}</span>
                {/* {note && (
                    <>
                        <span className={`TextUltraSmall ${styles.Label}`}>{t(dependencyNoteLabelKey[note.status])}</span>
                        <span className={`TextSmall ${styles.NoteValue}`}>{note.value}</span>
                    </>
                )} */}
                <span className={`TextUltraSmall ${styles.Label}`}>{t("DependenciesAnalyzer.detail.reference")}</span>

                {/* TODO: Fix in the future */}
                <span className={styles.Reference}>
                    <a
                        href={softwareProject.reference.url}
                        target="_blank"
                        rel="noopener noreferrer"
                        className={`TextSmall ${styles.ReferenceLink}`}
                    >
                        {softwareProject.reference.label}
                    </a>
                    <LinkIcon className={styles.ReferenceIcon} />
                </span>
            </div>
        </>
    );
};
