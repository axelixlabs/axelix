export interface IMetricsGroup {
    /**
     * Metrics Group нame
     */
    groupName: string;

    /**
     * List of metrics
     */
    metrics: string[];
}

export interface IMeasurement {
    /**
     * Statistic name
     */
    statistic: string;

    /**
     * Value of the measurement
     */
    value: number;
}

interface IAvailableTag {
    /**
     * Tag name
     */
    tag: string;

    /**
     * Available values for the tag
     */
    values: string[];
}

export interface IMetricsResponseBody {
    /**
     * List of metric groups
     */
    metricsGroups: IMetricsGroup[];
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
    baseUnit: string;

    /**
     * Measurements for the metric
     */
    measurements: IMeasurement[];

    /**
     * Tags available for this metric
     */
    availableTags: IAvailableTag[];
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
}
