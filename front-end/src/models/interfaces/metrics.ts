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

/**
 * Represents the possible value of the given metric along with the
 * validity flag.
 */
export interface ITagValueOptionValue {
    /**
     * Option value
     */
    value: string;

    /**
     * Whether this tag value is valid, considering other values of other tags.
     */
    invalid: boolean;
}

export interface ITagValueOption {
    /**
     * The name of the tag
     */
    tag: string;

    /**
     * Possible values for the tag with the given name
     */
    values: ITagValueOptionValue[];
}
