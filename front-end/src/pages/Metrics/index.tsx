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
        .map((metric) => ({
            value: metric,
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
                                    <Accordion header={<MetricHeader name={metric} />} key={metric}>
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
