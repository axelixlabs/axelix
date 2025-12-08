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
import { useEffect, useState } from "react";
import { useParams } from "react-router-dom";

import { Accordion, EmptyHandler, Loader, PageSearch } from "components";
import { fetchData, filterMetrics, findMetricsCount } from "helpers";
import { type IMetricsResponseBody, StatefulRequest } from "models";
import { getMetricsData } from "services";

import { MetricBody } from "./MetricBody";
import { MetricHeader } from "./MetricHeader";
import styles from "./styles.module.css";

const Metrics = () => {
    const { instanceId } = useParams();

    const [search, setSearch] = useState<string>("");

    const [metricsData, setMetricsData] = useState(StatefulRequest.loading<IMetricsResponseBody>());

    useEffect(() => {
        fetchData(setMetricsData, () => getMetricsData(instanceId!));
    }, []);

    if (metricsData.loading) {
        return <Loader />;
    }

    if (metricsData.error) {
        return <EmptyHandler isEmpty />;
    }

    const metricsGroups = metricsData.response!.metricsGroups;
    const effectiveMetricsGroups = search ? filterMetrics(metricsGroups, search) : metricsGroups;

    const totalMetricsCount = findMetricsCount(metricsGroups);
    const filteredMetricsCount = findMetricsCount(effectiveMetricsGroups);

    const addonAfter = `${filteredMetricsCount} / ${totalMetricsCount}`;

    const autocompleteOptions = effectiveMetricsGroups
        .flatMap((metrics) => metrics.metrics)
        .map(({ metricName }) => ({
            value: metricName,
        }));

    return (
        <>
            <PageSearch addonAfter={addonAfter} setSearch={setSearch} autocompleteOptions={autocompleteOptions} />

            <EmptyHandler isEmpty={!filteredMetricsCount}>
                {effectiveMetricsGroups.map(({ groupName, metrics }) => (
                    <div className={`AccordionsWrapper ${styles.AccordionsWrapper}`} key={groupName}>
                        <Accordion header={groupName} headerStyles={styles.HeaderStyles} accordionExpanded>
                            <div className="AccordionsWrapper">
                                {metrics.map((metric) => (
                                    <Accordion header={<MetricHeader metric={metric} />} key={metric.metricName}>
                                        <MetricBody metric={metric} />
                                    </Accordion>
                                ))}
                            </div>
                        </Accordion>
                    </div>
                ))}
            </EmptyHandler>
        </>
    );
};

export default Metrics;
