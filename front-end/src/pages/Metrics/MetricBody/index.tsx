import { Select } from "antd";
import { Fragment, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { useParams } from "react-router-dom";

import { EmptyHandler, Loader } from "components";
import {
    buildSelectedTagParams,
    extractUniqueMetricTagKeys,
    extractUniqueMetricValuesPerKey,
    fetchData,
} from "helpers";
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
        const selectedTagParams = buildSelectedTagParams(selectedTags);
        fetchData(setSingleMetricData, () =>
            getSingleMetricData({
                instanceId: instanceId!,
                metric: metric.metricName,
                tags: selectedTagParams,
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
    const singleMetricValidTagCombinations = singleMetricFeed.validTagCombinations;

    const validTagCombinations: IValidTagCombination[] = singleMetricValidTagCombinations;

    const uniqueTagKeys = extractUniqueMetricTagKeys(validTagCombinations);
    const valuesPerKey = extractUniqueMetricValuesPerKey(uniqueTagKeys, validTagCombinations, selectedTags);

    const handleSelectChange = (key: string, value?: string) => {
        setSelectedTags((prev) => {
            const next: Record<string, string> = { ...prev };

            if (value) {
                next[key] = value;
            } else {
                delete next[key];
            }

            const idx = uniqueTagKeys.indexOf(key);
            if (idx !== -1) {
                for (let i = idx + 1; i < uniqueTagKeys.length; i++) {
                    delete next[uniqueTagKeys[i]];
                }
            }

            return next;
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

                {!!uniqueTagKeys.length && (
                    <>
                        <div>{t("Metrics.tags")}:</div>
                        <div className={styles.TagsWrapper}>
                            {uniqueTagKeys.map((key, index) => {
                                const values = valuesPerKey[index] || [];
                                const prevKey = uniqueTagKeys[index - 1];
                                const disabled = index && !selectedTags[prevKey];
                                return (
                                    <Fragment key={key}>
                                        <div>{key}:</div>
                                        <Select
                                            value={selectedTags[key] || undefined}
                                            onChange={(value) => handleSelectChange(key, value)}
                                            placeholder={
                                                disabled ? t("Metrics.selectPrevFirst") : t("Metrics.selectValue")
                                            }
                                            options={values.map((value) => ({
                                                value: value,
                                            }))}
                                            disabled={disabled || !values.length}
                                            className={styles.TagSelect}
                                        />
                                    </Fragment>
                                );
                            })}
                        </div>
                    </>
                )}
            </div>

            <MetricChart measurements={singleMetricFeedMeasurements} />
        </div>
    );
};

export default MetricBody;
