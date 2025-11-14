import { Select } from "antd";
import { useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { useParams } from "react-router-dom";

import { EmptyHandler, Loader } from "components";
import { fetchData } from "helpers";
import { type ISingleMetricResponseBody, StatefulRequest } from "models";
import { getSingleMetricData } from "services";

import MetricChart from "../MetricChart";

import styles from "./styles.module.css";

interface IProps {
    /**
     * Metric name
     */
    metric: string;
}

export const MetricBody = ({ metric }: IProps) => {
    const { t } = useTranslation();
    const { instanceId } = useParams();

    const [singleMetricData, setSingleMetricData] = useState(StatefulRequest.loading<ISingleMetricResponseBody>());

    useEffect(() => {
        fetchData(setSingleMetricData, () =>
            getSingleMetricData({
                instanceId: instanceId!,
                metric: metric,
            }),
        );
    }, []);

    if (singleMetricData.loading) {
        return <Loader />;
    }

    if (singleMetricData.error) {
        return <EmptyHandler isEmpty />;
    }

    const singleMetricFeed = singleMetricData.response!;

    return (
        <div className={styles.MainWrapper}>
            <div className={styles.MetricDataWrapper}>
                <div>{t("Metrics.value")}:</div>
                <div>{singleMetricFeed.measurements.at(-1)?.value}</div>
                <div>{t("Metrics.baseUnit")}:</div>
                <div>{singleMetricFeed.baseUnit}</div>
                <div>{t("Metrics.tags")}:</div>
                <div className={styles.TagsWrapper}>
                    {singleMetricFeed.availableTags.map((availableTag) => (
                        <>
                            <div>{availableTag.tag}:</div>
                            <Select
                                placeholder={t("Metrics.selectTag")}
                                options={availableTag.values.map((value) => ({
                                    value: value,
                                    label: value,
                                }))}
                                className={styles.AvailableTagSelect}
                            />
                        </>
                    ))}
                </div>
            </div>
            <MetricChart measurements={singleMetricFeed.measurements} />
        </div>
    );
};
