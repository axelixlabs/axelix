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

import { PageSearch } from "components";
import type { IInstanceCard, IWallboardFilterEntity } from "models";

import { WallboardFilter } from "../WallboardFilter";

import styles from "./styles.module.css";

interface IProps {
    /**
     * Тext to display after the search field
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
    filters: IWallboardFilterEntity[];

    /**
     * SetState to update filters
     */
    setFilters: Dispatch<SetStateAction<IWallboardFilterEntity[]>>;
}

export const WallboardFirstSection = ({ addonAfter, setSearch, instanceCards, filters, setFilters }: IProps) => {
    const { t } = useTranslation();

    const [isPopoverOpen, setIsPopoverOpen] = useState<boolean>(false);

    const handleOpenChange = (newOpen: boolean): void => {
        setIsPopoverOpen(newOpen);
    };

    const removeWallboardFilter = (filterId: string): void => {
        setFilters((prevFilters) => prevFilters.filter((filter) => filter.id !== filterId));
    };

    return (
        <>
            <div className={styles.MainWrapper}>
                <PageSearch addonAfter={addonAfter} setSearch={setSearch} removeBottomGutter />
                <div className={styles.FiltersWrapper}>
                    {filters.map(({ technology, comparison, version, id }) => (
                        <Tag
                            closeIcon
                            color="#838383"
                            variant="outlined"
                            onClose={() => removeWallboardFilter(id)}
                            key={id}
                        >
                            {technology}: {comparison} {version}
                        </Tag>
                    ))}

                    <Popover
                        content={
                            <WallboardFilter
                                instanceCards={instanceCards}
                                setIsPopoverOpen={setIsPopoverOpen}
                                filters={filters}
                                setFilters={setFilters}
                            />
                        }
                        trigger="click"
                        placement="bottomLeft"
                        open={isPopoverOpen}
                        onOpenChange={handleOpenChange}
                        styles={{ container: { width: "500px" } }}
                    >
                        <Button size="small">+ {t("Wallboard.addFilter")}</Button>
                    </Popover>
                </div>
            </div>
        </>
    );
};
