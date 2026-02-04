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
import { Button, Select } from "antd";
import { type Dispatch, type SetStateAction, useState } from "react";
import { useTranslation } from "react-i18next";

import { getAllJavaVersions, getAllSpringBootVersions, getWallboardFilterVersions } from "helpers";
import type { IInstanceCard, IWallboardFilterEntity, IWallboardLocalFilterInitialState } from "models";
import { wallboardFilterComparisons, wallboardFilterTechnologies } from "utils";

import styles from "./styles.module.css";

interface IProps {
    /**
     * All services data
     */
    instanceCards: IInstanceCard[];

    /**
     * Setter for the popover open state.
     */
    setIsPopoverOpen: Dispatch<SetStateAction<boolean>>;

    /**
     * All filters data
     */
    filters: IWallboardFilterEntity[];

    /**
     * SetState to update filters
     */
    setFilters: Dispatch<SetStateAction<IWallboardFilterEntity[]>>;
}

const localFilterInitialState: IWallboardLocalFilterInitialState = {
    technology: null,
    comparison: null,
    version: null,
};

export const WallboardFilter = ({ instanceCards, setIsPopoverOpen, filters, setFilters }: IProps) => {
    const { t } = useTranslation();

    const [localFilter, setLocalFilter] = useState<IWallboardLocalFilterInitialState>(localFilterInitialState);
    const { technology, comparison, version } = localFilter;

    const javaVersions = getAllJavaVersions(instanceCards);
    const springBootVersions = getAllSpringBootVersions(instanceCards);

    const versions = !technology ? [] : technology === "Java" ? javaVersions : springBootVersions;

    const addFilter = (): void => {
        if (!technology || !comparison || !version) {
            // TODO: In the future, a validation error or another case will be shown
            return;
        }

        const filterId = `${technology}${comparison}${version}`;

        const isFilterExist = filters.some(({ id }) => id === filterId);

        if (isFilterExist) {
            // TODO: In the future, a validation error or another case will be shown
            return;
        }

        setFilters((prev) => [
            ...prev,
            {
                id: filterId,
                technology: technology,
                comparison: comparison,
                version: version,
            },
        ]);

        setIsPopoverOpen(false);

        setLocalFilter(localFilterInitialState);
    };

    const closePopover = (): void => {
        setIsPopoverOpen(false);
        setLocalFilter(localFilterInitialState);
    };

    return (
        <>
            <div className={styles.FieldAndComparisonWrapper}>
                <div className={styles.SelectWrapper}>
                    <label className={styles.Label}>{t("Wallboard.field")}</label>
                    <Select
                        placeholder={t("Wallboard.field")}
                        value={technology}
                        onChange={(value) => {
                            setLocalFilter((prev) => ({
                                ...prev,
                                technology: value,
                                version: null,
                            }));
                        }}
                        options={wallboardFilterTechnologies}
                    />
                </div>

                <div className={styles.SelectWrapper}>
                    <label className={styles.Label}>{t("Wallboard.comparison")}</label>
                    <Select
                        placeholder={t("Wallboard.comparison")}
                        value={comparison}
                        onChange={(value) => {
                            setLocalFilter((prev) => ({
                                ...prev,
                                comparison: value,
                            }));
                        }}
                        options={wallboardFilterComparisons(t)}
                    />
                </div>
            </div>

            <div className={styles.SelectWrapper}>
                <label className={styles.Label}>{t("value")}</label>
                <Select
                    placeholder={t("value")}
                    value={version}
                    onChange={(value) => {
                        setLocalFilter((prev) => ({
                            ...prev,
                            version: value,
                        }));
                    }}
                    options={getWallboardFilterVersions(versions)}
                />
            </div>

            <div className={styles.ActionsButtonsWrapper}>
                <Button onClick={closePopover}>{t("cancel")}</Button>
                <Button type="primary" onClick={addFilter}>
                    {t("Wallboard.save")}
                </Button>
            </div>
        </>
    );
};
