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
import { useEffect, useState } from "react";
import { useTranslation } from "react-i18next";

import { EmptyHandler, Loader, PagesFirstSection } from "@/components";
import { fetchData } from "@/helpers";
import { type IUpgradesData, StatefulRequest } from "@/models";
import { getUpgradesData } from "@/services";

import { UpgradesEmptyState } from "./UpgradesEmptyState";
import { UpgradesHeadroomPanel } from "./UpgradesHeadroomPanel";
import { UpgradesTable } from "./UpgradesTable";
import styles from "./styles.module.css";

const Upgrades = () => {
    const { t, i18n } = useTranslation();

    const [upgradesData, setUpgradesData] = useState(StatefulRequest.loading<IUpgradesData>());

    useEffect(() => {
        fetchData(setUpgradesData, () => getUpgradesData());
    }, []);

    if (upgradesData.loading) {
        return <Loader />;
    }

    if (upgradesData.error) {
        return <EmptyHandler isEmpty />;
    }

    const responseBody = upgradesData.response!;
    const { starterVersions, services, masterVersion, compatibilityWindow } = responseBody;

    return (
        <>
            <div className={styles.PageHeader}>
                <PagesFirstSection title={t("Upgrades.title")} subtitle={t("Upgrades.subtitle")} />

                <div className={styles.WindowCard} role="group" aria-labelledby="compatibility-window-title">
                    <div className={styles.WindowSize}>
                        <strong className="TextLarge">
                            {new Intl.NumberFormat(i18n.resolvedLanguage).format(compatibilityWindow)}
                        </strong>
                        <span className="TextUltraSmall">
                            {t("Upgrades.CompatibilityWindow.minorReleases", { count: compatibilityWindow })}
                        </span>
                    </div>
                    <div className={styles.WindowDetails}>
                        <strong id="compatibility-window-title">{t("Upgrades.CompatibilityWindow.title")}</strong>
                        <span className="TextUltraSmall">{t("Upgrades.CompatibilityWindow.description")}</span>
                    </div>
                </div>
            </div>

            {starterVersions.length > 0 ? (
                <>
                    <UpgradesHeadroomPanel data={responseBody} />

                    {services.length > 0 && <UpgradesTable services={services} />}
                </>
            ) : (
                <UpgradesEmptyState masterVersion={masterVersion} compatibilityWindow={compatibilityWindow} />
            )}
        </>
    );
};

export default Upgrades;
