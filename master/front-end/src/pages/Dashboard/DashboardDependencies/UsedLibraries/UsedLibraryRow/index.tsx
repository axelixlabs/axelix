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

import type { IUsedLibrary } from "@/models";
import { dependencyEcosystemLabelKey, dependencyHealthColor, riskyStatusOf, supportSignalLabelKey } from "@/utils";

import { HealthStatusBadge } from "../../HealthStatusBadge";

import { ShareCell } from "./ShareCell";
import { SuccessionCell } from "./SuccessionCell";
import { UsedLibraryDetail } from "./UsedLibraryDetail";
import styles from "./styles.module.css";

interface IProps {
    usedLibrary: IUsedLibrary;
}

export const UsedLibraryRow = ({ usedLibrary }: IProps) => {
    const { t } = useTranslation();

    const [open, setOpen] = useState<boolean>(false);

    const { library, status, succession, ecosystem, applicationCount, share } = usedLibrary;
    const riskyStatus = riskyStatusOf(status);

    return (
        <>
            <div className={`${styles.MainWrapper} ${open ? styles.Open : ""}`}>
                <div className={styles.Row} onClick={() => setOpen((prev) => !prev)}>
                    <span className={`TextUltraSmall ${styles.Caret}`}>{open ? "▴" : "▾"}</span>
                    <span className={`TextSmall ${styles.Library}`}>{`${library.groupId}:${library.artifactId}`}</span>
                    <span>
                        {riskyStatus && (
                            <HealthStatusBadge status={riskyStatus} label={t(supportSignalLabelKey[riskyStatus])} />
                        )}
                    </span>
                    <span className="TextUltraSmall">{riskyStatus && <SuccessionCell succession={succession} />}</span>
                    <span className={`TextUltraSmall ${styles.Category}`}>
                        {ecosystem && t(dependencyEcosystemLabelKey[ecosystem])}
                    </span>
                    <span className={`TextSmall ${styles.Applications}`}>{applicationCount}</span>
                    <ShareCell share={share} color={riskyStatus ? dependencyHealthColor[riskyStatus] : undefined} />
                </div>

                {open && <UsedLibraryDetail usedLibrary={usedLibrary} riskyStatus={riskyStatus} />}
            </div>
        </>
    );
};
