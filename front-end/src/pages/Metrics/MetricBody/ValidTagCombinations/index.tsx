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
import { Select } from "antd";
import type { DefaultOptionType } from "antd/es/select";
import { type Dispatch, Fragment, type SetStateAction } from "react";
import { useTranslation } from "react-i18next";

import { InfoTooltip } from "components";
import { getMetricTagValuesWithStatus } from "helpers";
import type { ITagValueOptionValue, IValidTagCombination } from "models";

import styles from "./styles.module.css";

interface IProps {
    /**
     * Single metric data
     */
    validTagCombinations: IValidTagCombination[];

    /**
     * Currently selected tags. Property key - selected tag name, property value - selected value.
     */
    selectedTags: Record<string, string>;

    /**
     * Setter for selected tags
     */
    setSelectedTags: Dispatch<SetStateAction<Record<string, string>>>;
}

export const ValidTagCombinations = ({ validTagCombinations, selectedTags, setSelectedTags }: IProps) => {
    const { t } = useTranslation();

    const tagValuesWithStatus = getMetricTagValuesWithStatus(validTagCombinations, selectedTags);

    const handleSelectChange = (tagName: string, selectedValue?: string): void => {
        setSelectedTags((prev) => {
            const updatedTags: Record<string, string> = { ...prev };

            if (selectedValue) {
                updatedTags[tagName] = selectedValue;
            } else {
                delete updatedTags[tagName];
            }

            return updatedTags;
        });
    };

    const createMetricTagSelectOptions = (values: ITagValueOptionValue[]): DefaultOptionType[] => {
        return values.map(({ value, invalid }) => ({
            label: invalid ? (
                <InfoTooltip text={t("Metrics.disabledTag")}>
                    <div>{value}</div>
                </InfoTooltip>
            ) : (
                value
            ),
            value: value,
            disabled: invalid,
        }));
    };

    if (validTagCombinations.length === 0) {
        return <></>;
    }

    return (
        <>
            <div>{t("Metrics.tags")}:</div>
            <div className={styles.TagsWrapper}>
                {tagValuesWithStatus.map(({ tag, values }) => (
                    <Fragment key={tag}>
                        <div>{tag}:</div>
                        <Select
                            value={selectedTags[tag] || undefined}
                            onChange={(it) => handleSelectChange(tag, it)}
                            placeholder={t("Metrics.selectValue")}
                            options={createMetricTagSelectOptions(values)}
                            allowClear
                            className={styles.TagSelect}
                            classNames={{
                                popup: {
                                    root: styles.SelectPopupRoot,
                                },
                            }}
                        />
                    </Fragment>
                ))}
            </div>
        </>
    );
};
