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
import { Fragment, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { useParams } from "react-router-dom";

import { EmptyHandler, InfoTooltip, Loader } from "components";
import { buildSelectedTagParams, fetchData, getMetricTagValuesWithStatus } from "helpers";
import {
    type IMetric,
    type ISingleMetricResponseBody,
    type ITagValueOptionValue,
    type IValidTagCombination,
    StatefulRequest,
} from "models";
import { getSingleMetricData } from "services";

import MetricChart from "../MetricChart";

import styles from "./styles.module.css";

interface IProps {
    /**
     * Single metric
     */
    metric: IMetric;
}

export const MetricBody = ({ metric }: IProps) => {
    const { t } = useTranslation();
    const { instanceId } = useParams();

    const [singleMetricData, setSingleMetricData] = useState(StatefulRequest.loading<ISingleMetricResponseBody>());
    const [selectedTags, setSelectedTags] = useState<Record<string, string>>({});

    useEffect(() => {
        setSingleMetricData(StatefulRequest.loading<ISingleMetricResponseBody>());

        fetchData(setSingleMetricData, () =>
            getSingleMetricData({
                instanceId: instanceId!,
                metric: metric.metricName,
                tags: buildSelectedTagParams(selectedTags),
            }),
        );
    }, [selectedTags]);

    if (singleMetricData.loading) {
        return <Loader />;
    }

    if (singleMetricData.error) {
        return <EmptyHandler isEmpty />;
    }

    const singleMetricFeed = singleMetricData.response!;
    const singleMetricFeedMeasurements = singleMetricFeed.measurements;
    const validTagCombinations: IValidTagCombination[] = singleMetricFeed.validTagCombinations;

    const tagValuesWithStatus = getMetricTagValuesWithStatus(validTagCombinations, selectedTags);

    const handleSelectChange = (tagName: string, selectedValue?: string) => {
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

    return (
        <div className={styles.MainWrapper}>
            <div className={styles.MetricDataWrapper}>
                <div>{t("Metrics.value")}:</div>
                <div>{singleMetricFeedMeasurements.at(-1)?.value}</div>

                {singleMetricFeed.baseUnit && (
                    <>
                        <div>{t("Metrics.baseUnit")}:</div>
                        <div>{singleMetricFeed.baseUnit}</div>
                    </>
                )}

                {validTagCombinations.length > 0 && (
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
                )}
            </div>

            <MetricChart measurements={singleMetricFeedMeasurements} />
        </div>
    );
};

export default MetricBody;
