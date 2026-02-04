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

import { EmptyHandler, Loader } from "components";
import { fetchData, filterInstances, filterWallboardInstances } from "helpers";
import { type IServiceCardsResponseBody, type IWallboardFilterEntity, StatefulRequest } from "models";
import { getWallboardData } from "services";

import { WallboardCard } from "./WallboardCard";
import { WallboardFirstSection } from "./WallboardFirstSection";
import styles from "./styles.module.css";

const Wallboard = () => {
    const [search, setSearch] = useState<string>("");

    const [filters, setFilters] = useState<IWallboardFilterEntity[]>([]);
    const [wallboard, setWallboard] = useState(StatefulRequest.loading<IServiceCardsResponseBody>());

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

    const filteredInstances = filters.length > 0 ? filterWallboardInstances(instanceCards, filters) : instanceCards;
    const effectiveInstanceCards = search ? filterInstances(filteredInstances, search) : filteredInstances;
    const addonAfter = `${effectiveInstanceCards.length} / ${instanceCards.length}`;

    return (
        <>
            <WallboardFirstSection
                addonAfter={addonAfter}
                setSearch={setSearch}
                instanceCards={instanceCards}
                filters={filters}
                setFilters={setFilters}
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
