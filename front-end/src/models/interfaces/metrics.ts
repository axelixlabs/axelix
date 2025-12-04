import { EStatisticType } from "../enums/metrics.ts";

export interface IMetric {
    /**
     * Metric name
     */
    metricName: string;

    /**
     * Metric description
     */
    description: string;
}

export interface IMetricsGroup {
    /**
     * Metrics Group нame
     */
    groupName: string;

    /**
     * List of metrics
     */
    metrics: IMetric[];
}

export interface IMeasurement {
    /**
     * Statistic name
     */
    statistic: EStatisticType;

    /**
     * Value of the measurement
     */
    value: number;
}

export interface IMetricsResponseBody {
    /**
     * List of metric groups
     */
    metricsGroups: IMetricsGroup[];
}

/**
 * Represents a valid combination of tags
 */
export interface IValidTagCombination {
    [key: string]: string;
}

export interface ISingleMetricResponseBody {
    /**
     * Metric name
     */
    name: string;

    /**
     * Metric description
     */
    description: string;

    /**
     * Base unit of the metric
     */
    baseUnit: string | null;

    /**
     * Measurements for the metric
     */
    measurements: IMeasurement[];

    /**
     * Represents a valid combination of tags
     */
    validTagCombinations: IValidTagCombination[];
}

export interface IGetSingleMetricRequestData {
    /**
     * Instance id of service
     */
    instanceId: string;

    /**
     * Metric name
     */
    metric: string;

    /**
     * List of selected tag params
     */
    tags: string[];
}

export interface ITagValueOption {
    /**
     * The name of the tag
     */
    tag: string;

    /**
     * Possible values for the tag with the given name
     */
    values: string[];
}
