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
import { apiFetch } from "api";
import type {
    IChangeLoggerGroupLevelRequestData,
    IResetLoggerLevelRequestData,
    ISetLoggerLevelRequestData,
} from "models";

export const getLoggersData = (instanceId: string) => {
    return apiFetch.get(`loggers/${instanceId}`);
};

export const setLoggerLevel = (data: ISetLoggerLevelRequestData) => {
    const { instanceId, loggerName, loggingLevel } = data;

    return apiFetch.post(`loggers/${instanceId}/logger/${loggerName}`, {
        configuredLevel: loggingLevel,
    });
};

export const resetLogger = (data: IResetLoggerLevelRequestData) => {
    const { instanceId, loggerName } = data;

    return apiFetch.post(`loggers/${instanceId}/logger/${loggerName}/reset`);
};

export const changeLoggerGroupLevel = (data: IChangeLoggerGroupLevelRequestData) => {
    const { instanceId, groupName, configuredLevel } = data;

    return apiFetch.post(`loggers/${instanceId}/group/${groupName}`, {
        configuredLevel: configuredLevel,
    });
};
