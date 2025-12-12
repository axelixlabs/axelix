/*
 * Copyright 2025-present, Nucleon Forge Software.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
    type ITagValueOptionValues,
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

    const createMetricTagSelectOptions = (values: ITagValueOptionValues[]): DefaultOptionType[] => {
        return values.map(({ value, disabled }) => ({
            label: disabled ? (
                <InfoTooltip text={t("Metrics.disabledTag")}>
                    <div>{value}</div>
                </InfoTooltip>
            ) : (
                value
            ),
            value: value,
            disabled: disabled,
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
