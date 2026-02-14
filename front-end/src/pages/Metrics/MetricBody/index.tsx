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
import { useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { useParams } from "react-router-dom";

import { EmptyHandler, Loader } from "components";
import { buildSelectedTagParams, createMeasurementsWithTimestamp, fetchData } from "helpers";
import { type IMeasurementsWithTimestamp, type IMetric, type ISingleMetricResponseBody, StatefulRequest } from "models";
import { getSingleMetricData } from "services";
import { METRIC_SHORT_POLLING_INTERVAL_MS, METRIC_SLIDING_WINDOW_MS } from "utils";

import { MetricChart } from "../MetricChart";

import { ValidTagCombinations } from "./ValidTagCombinations";
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

    const [latestMetricData, setLatestMetricData] = useState(StatefulRequest.loading<ISingleMetricResponseBody>());
    const [selectedTags, setSelectedTags] = useState<Record<string, string>>({});
    const [measurementsHistory, setMeasurementsHistory] = useState<IMeasurementsWithTimestamp[]>([]);
    const [startTime, setStartTime] = useState<number>(Date.now());

    useEffect(() => {
        setMeasurementsHistory([]);
        setStartTime(Date.now());
        setLatestMetricData(StatefulRequest.loading<ISingleMetricResponseBody>());

        const fetchMetricData = () => {
            fetchData(setLatestMetricData, () =>
                getSingleMetricData({
                    instanceId: instanceId!,
                    metric: metric.metricName,
                    tags: buildSelectedTagParams(selectedTags),
                }),
            );
        };

        fetchMetricData();

        const id = setInterval(() => {
            setMeasurementsHistory([]);
            setStartTime(Date.now());
        }, METRIC_SLIDING_WINDOW_MS);

        const intervalId = setInterval(fetchMetricData, METRIC_SHORT_POLLING_INTERVAL_MS);

        return () => {
            clearInterval(id);
            clearInterval(intervalId);
        };
    }, [selectedTags]);

    useEffect(() => {
        if (!latestMetricData.response) {
            return;
        }

        const measurements = latestMetricData.response.measurements;
        const measurementsWithTime = createMeasurementsWithTimestamp(measurements);

        setMeasurementsHistory((prev) => prev.concat(measurementsWithTime));
    }, [latestMetricData.response]);

    if (latestMetricData.loading) {
        return <Loader />;
    }

    if (latestMetricData.error) {
        return <EmptyHandler isEmpty />;
    }

    const singleMetricFeed = latestMetricData.response!;
    const measurementLastValue = measurementsHistory.at(-1)?.value;

    return (
        <div className={styles.MainWrapper}>
            <div className={styles.MetricDataWrapper}>
                <div>{t("value")}:</div>
                <div>{measurementLastValue}</div>

                {singleMetricFeed.baseUnit && (
                    <>
                        <div>{t("Metrics.baseUnit")}:</div>
                        <div>{singleMetricFeed.baseUnit}</div>
                    </>
                )}

                <ValidTagCombinations
                    selectedTags={selectedTags}
                    validTagCombinations={singleMetricFeed.validTagCombinations}
                    setSelectedTags={setSelectedTags}
                />
            </div>

            <MetricChart measurements={measurementsHistory} startTime={startTime} />
        </div>
    );
};
