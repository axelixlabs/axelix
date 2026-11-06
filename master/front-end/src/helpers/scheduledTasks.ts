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
import cronstrue from "cronstrue";

import "cronstrue/locales/ru";

import dayjs from "dayjs";
import type { TFunction } from "i18next";

import type { IScheduledTasksResponseBody, TScheduledTaskItem, TScheduledTaskType } from "@/models";

export const SCHEDULED_TASK_TYPE_LABEL_KEYS: Record<TScheduledTaskType, string> = {
    cron: "ScheduledTasks.cron",
    fixedDelay: "ScheduledTasks.fixedDelay",
    fixedRate: "ScheduledTasks.fixedRate",
};

export const filterScheduledTasks = (
    scheduledTasksResponse: IScheduledTasksResponseBody,
    search: string,
): IScheduledTasksResponseBody => {
    const formattedSearch = search.toLowerCase().trim();

    return {
        cron: scheduledTasksResponse.cron.filter((value) => value.taskId.toLowerCase().includes(formattedSearch)),

        fixedDelay: scheduledTasksResponse.fixedDelay.filter((value) =>
            value.taskId.toLowerCase().includes(formattedSearch),
        ),

        fixedRate: scheduledTasksResponse.fixedRate.filter((value) =>
            value.taskId.toLowerCase().includes(formattedSearch),
        ),
    };
};

export function isEmpty(resp: IScheduledTasksResponseBody): boolean {
    return resp.cron.length === 0 && resp.fixedDelay.length === 0 && resp.fixedRate.length === 0;
}

const getSimpleName = (qualifiedName: string): string => qualifiedName.slice(qualifiedName.lastIndexOf(".") + 1);

export const parseTaskId = (
    taskId: string,
): { method: string; suffix: string; className: string; qualifiedClassName: string } => {
    const [target, trigger] = taskId.split("#");
    const lambdaIndex = target.indexOf("$$Lambda");
    const methodSeparatorIndex = target.lastIndexOf(".");

    const qualifiedClassName = target.slice(0, lambdaIndex !== -1 ? lambdaIndex : Math.max(methodSeparatorIndex, 0));

    return {
        method: target.slice(lambdaIndex !== -1 ? lambdaIndex + 2 : methodSeparatorIndex + 1),
        suffix: trigger ? `#${trigger}` : "",
        className: getSimpleName(qualifiedClassName),
        qualifiedClassName,
    };
};

export const getScheduledTaskItems = ({
    cron,
    fixedDelay,
    fixedRate,
}: IScheduledTasksResponseBody): TScheduledTaskItem[] => [
    ...cron.map((task) => ({ type: "cron" as const, task })),
    ...fixedDelay.map((task) => ({ type: "fixedDelay" as const, task })),
    ...fixedRate.map((task) => ({ type: "fixedRate" as const, task })),
];

const RELATIVE_TIME_UNITS: [Intl.RelativeTimeFormatUnit, number][] = [
    ["day", 86400],
    ["hour", 3600],
    ["minute", 60],
    ["second", 1],
];

export const formatRelativeTime = (instant: string, language: string): string => {
    const elapsedSeconds = Math.max(dayjs().diff(instant, "second"), 0);
    const [unit, unitSeconds] = RELATIVE_TIME_UNITS.find(([, size]) => elapsedSeconds >= size) ?? ["second", 1];

    return new Intl.RelativeTimeFormat(language, { style: "short" }).format(
        -Math.round(elapsedSeconds / unitSeconds),
        unit,
    );
};

export const formatInterval = (intervalMillis: number, language: string): string => {
    const formatUnit = (value: number, unit: string): string =>
        new Intl.NumberFormat(language, {
            style: "unit",
            unit: unit,
            unitDisplay: "short",
            maximumFractionDigits: 3,
        }).format(value);

    if (intervalMillis < 1000) {
        return formatUnit(intervalMillis, "millisecond");
    }

    const parts: [number, string][] = [
        [Math.floor(intervalMillis / 3600000), "hour"],
        [Math.floor((intervalMillis % 3600000) / 60000), "minute"],
        [(intervalMillis % 60000) / 1000, "second"],
    ];

    return parts
        .filter(([value]) => value > 0)
        .map(([value, unit]) => formatUnit(value, unit))
        .join(" ");
};

export const getCronDescription = (cron: string, language: string, t: TFunction): string => {
    try {
        return cronstrue.toString(cron, { locale: language });
    } catch {
        return t("ScheduledTasks.editCronInvalidError");
    }
};
