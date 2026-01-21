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
import type {
    IMeasurement,
    IMeasurementsWithTimestamp,
    IMetricsGroup,
    ITagValueOption,
    ITagValueOptionValue,
    IValidTagCombination,
} from "models";
import { SHOW_RAW_THRESHOLD } from "utils";

import { commonNormalize } from "./globals";

export const reduceDisplayedNumber = (value: unknown): string => {
    if (value === null || value === undefined) {
        return "";
    }

    const numericValue = Number(value);
    const sign = numericValue < 0 ? "-" : "";
    const absoluteValue = Math.abs(numericValue);

    const trillion = 1_000_000_000_000;
    const billion = 1_000_000_000;
    const million = 1_000_000;
    const thousand = 1_000;

    if (absoluteValue < SHOW_RAW_THRESHOLD) {
        if (Number.isInteger(absoluteValue)) {
            return sign + String(absoluteValue);
        }

        return sign + (Math.trunc(absoluteValue * 100) / 100).toString();
    }

    if (absoluteValue >= trillion) {
        return sign + Number((absoluteValue / trillion).toFixed(2)).toString() + "T";
    }

    if (absoluteValue >= billion) {
        return sign + Number((absoluteValue / billion).toFixed(2)).toString() + "B";
    }

    if (absoluteValue >= million) {
        return sign + Number((absoluteValue / million).toFixed(2)).toString() + "M";
    }

    if (absoluteValue >= thousand) {
        return sign + Number((absoluteValue / thousand).toFixed(2)).toString() + "K";
    }

    return String(value);
};

export const filterMetrics = (metricsGroups: IMetricsGroup[], search: string): IMetricsGroup[] => {
    const formattedSearch = commonNormalize(search);

    return metricsGroups.reduce<IMetricsGroup[]>((result, metricsGroup) => {
        const { groupName, metrics } = metricsGroup;

        const isGroupNameMatch = groupName.includes(formattedSearch);

        if (isGroupNameMatch) {
            result.push(metricsGroup);
            return result;
        }

        const filteredMetrics = metrics.filter((metric) =>
            commonNormalize(metric.metricName).includes(formattedSearch),
        );

        if (filteredMetrics.length) {
            result.push({
                groupName: groupName,
                metrics: filteredMetrics,
            });
        }

        return result;
    }, []);
};

export const findMetricsCount = (metricsGroups: IMetricsGroup[]): number => {
    return metricsGroups.reduce((count, group) => count + group.metrics.length, 0);
};

const isValueMissed = (options: ITagValueOptionValue[], tagValue: string): boolean => {
    return !options.some(({ value }) => value === tagValue);
};

const createTagPossibleValues = (validTagCombinations: IValidTagCombination[]) => {
    const tagsPossibleValues = new Map<string, ITagValueOptionValue[]>();

    validTagCombinations.forEach((combination) => {
        Object.entries(combination).forEach(([tagKey, tagValue]) => {
            const options = tagsPossibleValues.get(tagKey) ?? [];

            if (isValueMissed(options, tagValue)) {
                options.push({
                    value: tagValue,
                    invalid: false,
                });
            }

            tagsPossibleValues.set(tagKey, options);
        });
    });

    return tagsPossibleValues;
};

const filterOtherSelected = (selectedEntries: [string, string][], tagKey: string): [string, string][] => {
    return selectedEntries.filter(([key]) => key !== tagKey);
};

const isValueValid = (
    validTagCombinations: IValidTagCombination[],
    tagKey: string,
    option: ITagValueOptionValue,
    otherSelected: [string, string][],
): boolean => {
    return validTagCombinations.some((combination) => {
        if (combination[tagKey] !== option.value) {
            return false;
        }

        return otherSelected.every(([key, value]) => combination[key] === value);
    });
};

export const getMetricTagValuesWithStatus = (
    validTagCombinations: IValidTagCombination[],
    selectedTags: IValidTagCombination,
): ITagValueOption[] => {
    const selectedEntries = Object.entries(selectedTags);

    const tagsPossibleValues = createTagPossibleValues(validTagCombinations);

    tagsPossibleValues.forEach((values, tag) => {
        values.forEach((value) => {
            const otherSelected = filterOtherSelected(selectedEntries, tag);

            value.invalid = !isValueValid(validTagCombinations, tag, value, otherSelected);
        });
    });

    return [...tagsPossibleValues.entries()].map(([tagName, tagValues]) => {
        const sortedTagValues = [...tagValues].sort((currentOption, nextOption) => {
            if (currentOption.invalid === nextOption.invalid) {
                return 0;
            }

            return currentOption.invalid ? 1 : -1;
        });

        return {
            tag: tagName,
            values: sortedTagValues,
        };
    });
};

export const buildSelectedTagParams = (selectedTags: Record<string, string>): string[] => {
    return Object.entries(selectedTags).map(([key, value]) => `${key}:${value}`);
};

/**
 * @param timestamp the unix timestamp (time im millisecond from epoch)
 */
export const formatXAxis = (timestamp: number): string => {
    const date = new Date(timestamp);

    // TODO: Consider the option of correct time display
    // use default locale by passing undefied
    return date.toLocaleTimeString(undefined, {
        hour: "2-digit",
        minute: "2-digit",
        second: "2-digit",
    });
};

export const getMetricsChartTicks = (startTime: number, endTime: number): number[] => {
    const window = endTime - startTime;

    /* eslint-disable */
    return [
        startTime,
        startTime + window * 0.25,
        startTime + window * 0.5,
        startTime + window * 0.75,
        endTime,
    ];
    /* eslint-enable */
};

export const createMeasurementsWithTimestamp = (measurements: IMeasurement[]): IMeasurementsWithTimestamp[] => {
    return measurements.map(({ value }) => ({
        value: value,
        timestamp: Date.now(),
    }));
};
