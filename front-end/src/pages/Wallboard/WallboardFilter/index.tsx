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

import type { IInstanceCard, IWallboardLocalFilterInitialState, IWallboardSingleOperandFilter } from "models";
import { filteringKeys, getWallboardFilterDefinitions } from "utils";

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
    filters: IWallboardSingleOperandFilter[];

    /**
     * SetState to update filters
     */
    setFilters: Dispatch<SetStateAction<IWallboardSingleOperandFilter[]>>;
}

const localFilterInitialState: IWallboardLocalFilterInitialState = {
    key: null,
    operator: null,
    operand: null,
};

export const WallboardFilter = ({ instanceCards, setIsPopoverOpen, filters, setFilters }: IProps) => {
    const { t } = useTranslation();

    const [localFilter, setLocalFilter] = useState<IWallboardLocalFilterInitialState>(localFilterInitialState);
    const { key, operator, operand } = localFilter;

    const addFilter = (): void => {
        if (!key || !operator || !operand) {
            // TODO: In the future, a validation error or another case will be shown
            return;
        }

        const filterId = `${key}${operator}${operand}`;

        const isFilterExist = filters.some(({ id }) => id === filterId);

        if (isFilterExist) {
            // TODO: In the future, a validation error or another case will be shown
            return;
        }

        setFilters((prev) => [
            ...prev,
            {
                id: filterId,
                key: key,
                operator: operator,
                operand: operand,
            },
        ]);

        setIsPopoverOpen(false);

        setLocalFilter(localFilterInitialState);
    };

    const closePopover = (): void => {
        setIsPopoverOpen(false);
        setLocalFilter(localFilterInitialState);
    };

    const currentFilterDefinition = key ? getWallboardFilterDefinitions(t)[key] : undefined;
    const operatorOptions = currentFilterDefinition?.operators ?? [];
    const operandOptions = currentFilterDefinition?.getSelectOptionsData(instanceCards) ?? [];

    return (
        <>
            <div className={styles.FieldAndComparisonWrapper}>
                <div className={styles.SelectWrapper}>
                    <label className={styles.Label}>{t("Wallboard.filter.field")}</label>
                    <Select
                        placeholder={t("Wallboard.filter.field")}
                        value={key}
                        onChange={(key) => {
                            setLocalFilter({
                                operator: null,
                                key: key,
                                operand: null,
                            });
                        }}
                        options={filteringKeys}
                    />
                </div>

                <div className={styles.SelectWrapper}>
                    <label className={styles.Label}>{t("Wallboard.filter.comparison")}</label>
                    <Select
                        placeholder={t("Wallboard.filter.comparison")}
                        value={operator}
                        onChange={(operator) => {
                            setLocalFilter((prev) => ({
                                ...prev,
                                operator: operator,
                            }));
                        }}
                        options={operatorOptions}
                    />
                </div>
            </div>

            <div className={styles.SelectWrapper}>
                <label className={styles.Label}>{t("value")}</label>
                <Select
                    placeholder={t("value")}
                    value={operand}
                    onChange={(operand) => {
                        setLocalFilter((prev) => ({
                            ...prev,
                            operand: operand,
                        }));
                    }}
                    options={operandOptions}
                />
            </div>

            <div className={styles.ActionsButtonsWrapper}>
                <Button onClick={closePopover}>{t("cancel")}</Button>
                <Button type="primary" onClick={addFilter}>
                    {t("Wallboard.filter.save")}
                </Button>
            </div>
        </>
    );
};
