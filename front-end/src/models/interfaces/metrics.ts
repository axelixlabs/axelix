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

export interface IMeasurementsWithTimestamp extends IMeasurement {
    timestamp: number;
}
