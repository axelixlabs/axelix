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

interface ITarget {
    /**
     * Target field of runnable field
     */
    target: string;
}

export interface IRunnable {
    /**
     * Runnable field of scheduled tasks types body
     */
    runnable: ITarget;

    /**
     * Whether the given runnable is enabled or not
     */
    enabled: boolean;
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

export interface IUpdateCronExpressionDataRequestData {
    /**
     * Instance id of service
     */
    instanceId: string;

    /**
     * The trigger whose expression we are changing
     */
    trigger: string;

    /**
     * New cron expression
     */
    newCronExpression: string;
}

export interface IForceRunRequestData {
    /**
     * Instance id of service
     */
    instanceId: string;

    /**
     * The trigger that we force to run
     */
    trigger: string;
}

export interface IChangeScheduledTaskIntervalRequestData {
    /**
     * Instance id of service
     */
    instanceId: string;

    /**
     * The trigger whose interval we are changing
     */
    trigger: string;

    /**
     * The new interval
     */
    interval: number;
}
