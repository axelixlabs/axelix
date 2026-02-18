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
import { Button, Popover, Tag } from "antd";
import { type Dispatch, type SetStateAction, useState } from "react";
import { useTranslation } from "react-i18next";
import type { SetURLSearchParams } from "react-router-dom";

import { PageSearch } from "components";
import { getFilteredWallboardFilters } from "helpers";
import type { IInstanceCard, IWallboardSingleOperandFilter } from "models";
import { SEARCH_PARAMS_FILTER } from "utils";

import { WallboardFilter } from "../WallboardFilter";

import styles from "./styles.module.css";

interface IProps {
    /**
     * Text to display after the search field
     */
    addonAfter: string;

    /**
     * SetState to update the search string
     */
    setSearch: Dispatch<SetStateAction<string>>;

    /**
     * All services data
     */
    instanceCards: IInstanceCard[];

    /**
     * All filters data
     */
    parsedFilters: IWallboardSingleOperandFilter[];

    /**
     * Function to update the current URL search parameters.
     */
    setSearchParams: SetURLSearchParams;

    /**
     * Current URL search parameters object.
     */
    searchParams: URLSearchParams;
}

export const WallboardFirstSection = ({
    addonAfter,
    setSearch,
    instanceCards,
    parsedFilters,
    searchParams,
    setSearchParams,
}: IProps) => {
    const { t } = useTranslation();

    const [isPopoverOpen, setIsPopoverOpen] = useState<boolean>(false);

    const handleOpenChange = (newOpen: boolean): void => {
        setIsPopoverOpen(newOpen);
    };

    const removeFilter = (id: string): void => {
        const remainingFilters = getFilteredWallboardFilters(searchParams, id);

        searchParams.delete(SEARCH_PARAMS_FILTER);
        remainingFilters.forEach((filter) => searchParams.append(SEARCH_PARAMS_FILTER, filter));

        setSearchParams(searchParams, { replace: true });
    };

    return (
        <>
            <div className={styles.MainWrapper}>
                <PageSearch addonAfter={addonAfter} setSearch={setSearch} removeBottomGutter />
                <div className={styles.FiltersWrapper}>
                    {parsedFilters.map(({ key, operator, operand, id }) => (
                        <Tag closeIcon color="#838383" variant="outlined" onClose={() => removeFilter(id)} key={id}>
                            {key} {operator} {operand}
                        </Tag>
                    ))}

                    <Popover
                        content={
                            <WallboardFilter
                                instanceCards={instanceCards}
                                setIsPopoverOpen={setIsPopoverOpen}
                                parsedFilters={parsedFilters}
                                searchParams={searchParams}
                                setSearchParams={setSearchParams}
                            />
                        }
                        trigger="click"
                        placement="bottomLeft"
                        open={isPopoverOpen}
                        onOpenChange={handleOpenChange}
                        styles={{ container: { width: "500px" } }}
                    >
                        <Button size="small">+ {t("Wallboard.filter.addFilter")}</Button>
                    </Popover>
                </div>
            </div>
        </>
    );
};
