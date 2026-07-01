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
export interface ILogger {
    /**
     * Logger name
     */
    name: string;

    /**
     * Explicitly configured level for logger, if any
     */
    configuredLevel?: string;

    /**
     * Single logger current level
     */
    effectiveLevel: string;

    temporaryLevelInitiatedAt: string | null;
    temporaryLevelRollsBackAt: string | null;
}

export interface ILoggerGroup {
    /**
     * The name of a logger group
     */
    name: string;

    /**
     * The configured level of a logger group
     */
    configuredLevel?: string;

    /**
     * Members of a logger group
     */
    members: string[];
}

export interface ILoggersResponseBody {
    /**
     * All logger groups data
     */
    groups: Record<string, Omit<ILoggerGroup, "name">>;

    /**
     * All possible logging levels that are supported by the logging system inside the instance
     */
    levels: string[];

    /**
     * All loggers
     */
    loggers: Record<string, Omit<ILogger, "name">>;
}

export interface ISetLoggerLevelRequestData {
    /**
     * Instance id
     */
    instanceId: string;

    /**
     * Logger name
     */
    loggerName: string;

    /**
     * Selected level
     */
    loggingLevel: string;
    ttlSeconds?: number;
}

export interface IResetLoggerLevelRequestData {
    /**
     * Instance id
     */
    instanceId: string;

    /**
     * Logger name
     */
    loggerName: string;
}

export interface IChangeLoggerGroupLevelRequestData {
    /**
     * Instance id of service
     */
    instanceId: string;

    /**
     * The name of a logger group
     */
    groupName: string;

    /**
     * The configured level of a logger group
     */
    configuredLevel: string;
}

export interface ITimepickerData {
    hour: string;
    minutes: string;
    type?: string;
    time: string;
    degreesHours: number | null;
    degreesMinutes: number | null;
}

export interface ITimepickerClockConfig {
    type: "12h" | "24h";
    locale: string;
}