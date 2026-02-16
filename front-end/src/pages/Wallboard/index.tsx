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
import { fetchData, filterWallboardInstances } from "helpers";
import { type IServiceCardsResponseBody, type IWallboardSingleOperandFilter, StatefulRequest } from "models";
import { getWallboardData } from "services";

import { WallboardCard } from "./WallboardCard";
import { WallboardFirstSection } from "./WallboardFirstSection";
import styles from "./styles.module.css";

const Wallboard = () => {
    const { t } = useTranslation();
    const [searchParams, setSearchParams] = useSearchParams();

    const [search, setSearch] = useState<string>("");
    const [wallboard, setWallboard] = useState(StatefulRequest.loading<IServiceCardsResponseBody>());

    const [filters, setFilters] = useState<IWallboardSingleOperandFilter[]>(() => {
        const filtersFromUrl = searchParams.getAll("f");

        if (filtersFromUrl.length === 0) {
            return [];
        }

        return filtersFromUrl.map((filter) => {
            const [key, operator, operand] = filter.split(":");
            const filterId = `${key}${operator}${operand}`;
            return { id: filterId, key, operator, operand } as IWallboardSingleOperandFilter;
        });
    });

    useEffect(() => {
        fetchData(setWallboard, () => getWallboardData());
    }, []);

    useEffect(() => {
        const params = new URLSearchParams(searchParams);

        params.delete("f");

        filters.forEach((filter) => {
            const filterString = `${filter.key}:${filter.operator}:${filter.operand}`;
            params.append("f", filterString);
        });

        setSearchParams(params, { replace: true });
    }, [filters]);

    if (wallboard.loading) {
        return <Loader />;
    }

    if (wallboard.error) {
        return <EmptyHandler isEmpty />;
    }

    const instanceCards = wallboard.response!.instances;

    /* eslint-disable */
    const effectiveInstanceCards =
        (filters.length > 0 || search)
            ? filterWallboardInstances(instanceCards, search, filters, t)
            : instanceCards;
    /* eslint-enable */

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
