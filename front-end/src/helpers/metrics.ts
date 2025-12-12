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
import type { IMetricsGroup, ITagValueOption, ITagValueOptionValues, IValidTagCombination } from "models";
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

export const getMetricTagValuesWithStatus = (
    validTagCombinations: IValidTagCombination[],
    selectedTags: IValidTagCombination,
): ITagValueOption[] => {
    const selectedEntries = Object.entries(selectedTags);

    const tagsPossibleValues = new Map<string, ITagValueOptionValues[]>();

    validTagCombinations.forEach((combination) => {
        Object.entries(combination).forEach(([tagKey, tagValue]) => {
            const options = tagsPossibleValues.get(tagKey) ?? [];

            if (!options.some(({ value }) => value === tagValue)) {
                options.push({
                    value: tagValue,
                    disabled: false,
                });
            }

            tagsPossibleValues.set(tagKey, options);
        });
    });

    tagsPossibleValues.forEach((optionsForTag, tagKey) => {
        optionsForTag.forEach((option) => {
            const otherSelected = selectedEntries.filter(([key]) => key !== tagKey);

            const isEnabled = validTagCombinations.some((combination) => {
                if (combination[tagKey] !== option.value) {
                    return false;
                }

                return otherSelected.every(
                    ([selectedKey, selectedValue]) => combination[selectedKey] === selectedValue,
                );
            });

            option.disabled = !isEnabled;
        });
    });

    return [...tagsPossibleValues.entries()].map(([tagName, tagValues]) => {
        const sortedTagValues = [...tagValues].sort((currentOption, nextOption) => {
            if (currentOption.disabled === nextOption.disabled) {
                return 0;
            }

            return currentOption.disabled ? 1 : -1;
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

export const createMetricTagSelectOptions = (values: ITagValueOptionValues[]) => {
    return values.map(({ value, disabled }) => ({
        value: value,
        disabled: disabled,
    }));
};
