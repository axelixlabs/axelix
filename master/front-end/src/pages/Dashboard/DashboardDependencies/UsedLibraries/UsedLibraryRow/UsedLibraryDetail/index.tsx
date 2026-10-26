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
import { Tabs } from "antd";
import { useTranslation } from "react-i18next";

import type { IUsedLibrary, TProblemSupportStatus } from "@/models";

import { LibraryStatus } from "./LibraryStatus";
import { LibraryVersions } from "./LibraryVersions";
import styles from "./styles.module.css";

interface IProps {
    usedLibrary: IUsedLibrary;

    /**
     * The status of the library when it is a risky one. Only those have a Status tab.
     */
    riskyStatus: TProblemSupportStatus | null;
}

export const UsedLibraryDetail = ({ usedLibrary, riskyStatus }: IProps) => {
    const { t } = useTranslation();

    const statusTab = {
        key: "status",
        label: t("Dashboard.Dependencies.detail.statusTab"),
        children: <LibraryStatus usedLibrary={usedLibrary} />,
    };

    const versionsTab = {
        key: "versions",
        label: (
            <span className={styles.TabLabel}>
                {t("Dashboard.Dependencies.detail.versionsTab")}
                <span className={styles.TabCount}>{usedLibrary.versions.length}</span>
            </span>
        ),
        children: <LibraryVersions usedLibrary={usedLibrary} />,
    };

    return (
        <>
            <div className={styles.MainWrapper}>
                <Tabs
                    size="small"
                    defaultActiveKey={riskyStatus ? statusTab.key : versionsTab.key}
                    items={riskyStatus ? [statusTab, versionsTab] : [versionsTab]}
                />
            </div>
        </>
    );
};
