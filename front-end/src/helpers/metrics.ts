import type { IMetricsGroup, ITagValueOption, IValidTagCombination } from "models";
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

/**
 * Returns possible tag values options. Contains possible values for all the tags.
 *
 * If the given tag already has a selected value, then the array of possible values for the given tag
 * will contain the only value - the selected value.
 *
 * This would make it impossible to distinguish two cases:
 * - The selected that already has a selected value
 * - Tag that possibly can have only a single value (considering the values fo other tags of course)
 *
 * But we do not need to distinguish these two cases.
 *
 * @param validTagCombinations valid tag combinations.
 * @param selectedTags tags that are currently selected.
 */
export const extractUniqueMetricValuesPerKey = (
    validTagCombinations: IValidTagCombination[],
    selectedTags: IValidTagCombination,
): ITagValueOption[] => {
    const selected = Object.entries(selectedTags);

    const tagsPossibleValues = new Map<string, string[]>();

    validTagCombinations
        .filter((combination) => {
            return selected.every(([key, value]) => combination[key] === value);
        })
        .forEach((combination) => {
            Object.entries(combination).forEach(([tag, val]) => {
                const values = tagsPossibleValues.get(tag) ?? [];
                if (!values.includes(val)) {
                    values.push(val);
                }
                tagsPossibleValues.set(tag, values);
            });
        });

    return [...tagsPossibleValues.entries()].map(([tagName, tagValues]) => {
        return {
            tag: tagName,
            values: tagValues,
        };
    });
};

export const buildSelectedTagParams = (selectedTags: Record<string, string>): string[] => {
    return Object.entries(selectedTags).map(([key, value]) => `${key}:${value}`);
};
