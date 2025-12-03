import { Select } from "antd";
import { Fragment, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { useParams } from "react-router-dom";

import { EmptyHandler, Loader } from "components";
import { buildSelectedTagParams, extractUniqueMetricValuesPerKey, fetchData } from "helpers";
import { type IMetric, type ISingleMetricResponseBody, type IValidTagCombination, StatefulRequest } from "models";
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

    const valuesPerKey = extractUniqueMetricValuesPerKey(validTagCombinations, selectedTags);

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

                {validTagCombinations.length && (
                    <>
                        <div>{t("Metrics.tags")}:</div>
                        <div className={styles.TagsWrapper}>
                            {Object.entries(valuesPerKey).map(([tagName, possibleValues]) => (
                                <Fragment key={tagName}>
                                    <div>{tagName}:</div>
                                    <Select
                                        value={selectedTags[tagName] || undefined}
                                        onChange={(value) => handleSelectChange(tagName, value)}
                                        placeholder={t("Metrics.selectValue")}
                                        options={possibleValues.map((value) => ({
                                            value: value,
                                        }))}
                                        allowClear
                                        className={styles.TagSelect}
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
