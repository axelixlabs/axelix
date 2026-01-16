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
import type { IEnableGCLoggingRequestData } from "models";

export const getGCLoggingStatus = (instanceId: string) => {
    return apiFetch.get(`garbage-collector/logs/${instanceId}/status`);
};

export const enableGCLogging = (data: IEnableGCLoggingRequestData) => {
    const { instanceId, level } = data;

    return apiFetch.post(`garbage-collector/logs/${instanceId}/enable`, {
        level: level,
    });
};

export const triggerGC = (instanceId: string) => {
    return apiFetch.post(`garbage-collector/${instanceId}/trigger`);
};

export const disableGCLogging = (instanceId: string) => {
    return apiFetch.post(`garbage-collector/logs/${instanceId}/disable`);
};

export const getGCLogFile = (instanceId: string) => {
    return apiFetch.get(`garbage-collector/logs/${instanceId}/file`);
};
