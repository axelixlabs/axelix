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
import { useSearchParams } from "react-router-dom";

import { EmptyHandler, Loader } from "components";
import { fetchData, filterWallboardInstances, parseWallboardFilters } from "helpers";
import { type IServiceCardsResponseBody, StatefulRequest } from "models";
import { getWallboardData } from "services";

import { WallboardCard } from "./WallboardCard";
import { WallboardFirstSection } from "./WallboardFirstSection";
import styles from "./styles.module.css";

const Wallboard = () => {
    const { t } = useTranslation();
    const [searchParams, setSearchParams] = useSearchParams();

    const [search, setSearch] = useState<string>("");
    const [wallboard, setWallboard] = useState(StatefulRequest.loading<IServiceCardsResponseBody>());

    const parsedFilters = parseWallboardFilters(searchParams);

    useEffect(() => {
        fetchData(setWallboard, () => getWallboardData());
    }, []);

    if (wallboard.loading) {
        return <Loader />;
    }

    if (wallboard.error) {
        return <EmptyHandler isEmpty />;
    }

    const instanceCards = wallboard.response!.instances;

    const hasSearchOrFilters = parsedFilters.length > 0 || search;
    const effectiveInstanceCards = hasSearchOrFilters
        ? filterWallboardInstances(instanceCards, search, parsedFilters, t)
        : instanceCards;

    const addonAfter = `${effectiveInstanceCards.length} / ${instanceCards.length}`;

    return (
        <>
            <WallboardFirstSection
                addonAfter={addonAfter}
                setSearch={setSearch}
                instanceCards={instanceCards}
                parsedFilters={parsedFilters}
                searchParams={searchParams}
                setSearchParams={setSearchParams}
            />

            <EmptyHandler isEmpty={effectiveInstanceCards.length === 0}>
                <div className={styles.CardsResponsiveWrapper}>
                    {effectiveInstanceCards.map((data) => (
                        <WallboardCard data={data} key={data.instanceId} />
                    ))}
                </div>
            </EmptyHandler>
        </>
    );
};

export default Wallboard;
