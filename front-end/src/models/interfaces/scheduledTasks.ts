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
import type { ICommonSliceState } from "./globals";

interface IEnable {
    enabled: boolean;
}

interface ITarget {
    /**
     * Target field of runnable field
     */
    target: string;
}

export interface IRunnable extends IEnable {
    /**
     * Runnable field of scheduled tasks types body
     */
    runnable: ITarget;
}

/**
 * Body chunk of cron task type
 */
export interface ICron extends IRunnable {
    expression: string;
}

/**
 * Body chunk of fixedDelay or fixedRate tasks types
 */
export interface IFixedTasks extends IRunnable {
    interval: number;
    initialDelay: number;
}

// TODO: See the comment: https://github.com/Nucleon-Forge/axelix/pull/205#discussion_r2431398757
export interface IScheduledTaskItem {
    // TODO: migrate to enum later in the future
    /**
     * Scheduled tasks type
     */
    type: "cron" | "fixedDelay" | "fixedRate" | "custom";
    /**
     * Scheduled tasks list
     */
    tasks: ICron[] | IFixedTasks[];
}

export interface IScheduledTasksSliceState extends ICommonSliceState {
    /**
     * Scheduled tasks data after changes in frontend for better comfort
     */
    scheduledTasksTypes: IScheduledTaskItem[];
}

/**
 * Initial scheduled tasks response data
 */
export interface IScheduledTasksResponseBody {
    cron: ICron[];
    fixedDelay: IFixedTasks[];
    fixedRate: IFixedTasks[];
}

export interface IUpdateScheduledTasksStatusRequestData {
    instanceId: string;
    targetScheduledTask: string;
    force: boolean;
    statusType: "enable" | "disable";
}
