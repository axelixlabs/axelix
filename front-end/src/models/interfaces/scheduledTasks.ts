import type { ICommonSliceState } from "./globals";

interface ITarget {
    /**
     * Target field of runnable field
     */
    target: string;
}

interface IRunnable {
    /**
     * Runnable field of scheduled tasks types body
     */
    runnable: ITarget
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

// TODO: See the comment: https://github.com/Nucleon-Forge/axile/pull/205#discussion_r2431398757
export interface IScheduledTaskItem {

    // TODO: migrate to enum later in the future
    /**
     * Scheduled tasks type
     */
    type: "cron" | "fixedDelay" | "fixedRate" | "custom",
    /**
     * Scheduled tasks list
     */
    tasks: ICron[] | IFixedTasks[]
}

export interface IScheduledTasksSliceState extends ICommonSliceState {
    /**
     * Scheduled tasks data after changes in frontend for better comfort
     */
    scheduledTasksTypes: IScheduledTaskItem[];
    /**
     * Search text
     */
    scheduledTasksSearchText: string;
    /**
     * Filtered scheduled tasks
     */
    filteredScheduledTasksTypes: IScheduledTaskItem[]
}

/**
 * Initial scheduled tasks response data
 */
export interface ResponseData {
    cron: ICron[];
    fixedDelay: IFixedTasks[];
    fixedRate: IFixedTasks[]
}