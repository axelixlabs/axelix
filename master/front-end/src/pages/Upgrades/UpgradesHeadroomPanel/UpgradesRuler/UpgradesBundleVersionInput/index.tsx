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
import { Input, Skeleton } from "antd";
import { useEffect, useState } from "react";
import { useTranslation } from "react-i18next";

import { fetchData } from "@/helpers";
import { type IUpgradesLostApplicationsData, StatefulRequest } from "@/models";
import { getUpgradesLostApplications } from "@/services";

import styles from "./styles.module.css";

interface IProps {
    masterVersion: string;
    dropVersion: string;
    oldestStarterVersion: string;
    oldestStarterServiceCount: number;
}

export const UpgradesBundleVersionInput = ({
    masterVersion,
    dropVersion,
    oldestStarterVersion,
    oldestStarterServiceCount,
}: IProps) => {
    const { t } = useTranslation();

    const [targetVersion, setTargetVersion] = useState<string>("");

    const [upgradesLostApplications, setUpgradesLostApplications] = useState<
        StatefulRequest<IUpgradesLostApplicationsData>
    >(new StatefulRequest<IUpgradesLostApplicationsData>(false, "", null));

    useEffect(() => {
        if (!targetVersion) {
            return;
        }

        const loadLostApplications = (): void => {
            fetchData(setUpgradesLostApplications, () => getUpgradesLostApplications(targetVersion));
        };

        setUpgradesLostApplications(StatefulRequest.loading());

        const timeout = setTimeout(loadLostApplications, 500);

        return () => clearTimeout(timeout);
    }, [targetVersion]);

    return (
        <>
            <div className={`TextUltraSmall ${styles.MainWrapper}`}>
                <div className={styles.Label}>{t("Upgrades.BundleVersionInput.versionLabel")}</div>
                <Input
                    size="small"
                    placeholder={masterVersion}
                    onChange={(e) => setTargetVersion(e.target.value)}
                    className={styles.Input}
                />

                {targetVersion &&
                    (upgradesLostApplications.loading ? (
                        <Skeleton.Input active size="small" className={styles.HintSkeleton} />
                    ) : upgradesLostApplications.response ? (
                        <span className={styles.BundleInputHint}>
                            {upgradesLostApplications.response.lostApplications === 0
                                ? t("Upgrades.BundleVersionInput.noServicesFound")
                                : t("Upgrades.BundleVersionInput.allServicesSupported", {
                                      count: upgradesLostApplications.response.lostApplications,
                                  })}
                        </span>
                    ) : null)}
            </div>

            <div className={`TextUltraSmall ${styles.Note}`}>
                {t("Upgrades.BundleVersionInput.versionNote", {
                    version: dropVersion,
                    count: oldestStarterServiceCount,
                    starter: oldestStarterVersion,
                })}
            </div>
        </>
    );
};
