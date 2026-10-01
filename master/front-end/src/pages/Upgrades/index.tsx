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
import { UpgradesFleetBars } from "./UpgradesFleetBars";
import { UpgradesHeadroomPanel } from "./UpgradesHeadroomPanel";
import { UpgradesNoBlockersState } from "./UpgradesNoBlockersState";
import { UpgradesTable } from "./UpgradesTable";

const Upgrades = () => {
    const { t } = useTranslation();

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
    const { starterVersions, ceilingBlockers, masterVersion, compatibilityWindow } = responseBody;

    const isEmpty = starterVersions.length === 0;
    const isFleetUpToDate = ceilingBlockers.length === 0;

    return (
        <>
            <PagesFirstSection title={t("Upgrades.title")} subtitle={t("Upgrades.subtitle")} />

            {isEmpty ? (
                <UpgradesEmptyState masterVersion={masterVersion} compatibilityWindow={compatibilityWindow} />
            ) : isFleetUpToDate ? (
                <UpgradesNoBlockersState data={responseBody} />
            ) : (
                <>
                    <UpgradesHeadroomPanel data={responseBody} />

                    <UpgradesTable ceilingBlockers={ceilingBlockers} />

                    <UpgradesFleetBars data={responseBody} isFleetUpToDate={false} />
                </>
            )}
        </>
    );
};

export default Upgrades;
