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
    IChangeScheduledTaskIntervalRequestData,
    IForceRunRequestData,
    IUpdateCronExpressionDataRequestData,
    IUpdateScheduledTasksStatusRequestData,
} from "models";

export const getScheduledTasksData = (instanceId: string) => {
    return apiFetch.get(`scheduled-tasks/${instanceId}`);
};

export const updateScheduledTasksStatus = (data: IUpdateScheduledTasksStatusRequestData) => {
    const { instanceId, statusType, trigger, force } = data;

    return apiFetch.post(`scheduled-tasks/${instanceId}/${statusType}`, {
        trigger: trigger,
        force: force,
    });
};

export const changeCronExpression = (data: IUpdateCronExpressionDataRequestData) => {
    const { instanceId, newCronExpression, trigger } = data;

    return apiFetch.post(`scheduled-tasks/${instanceId}/modify/cron-expression`, {
        cronExpression: newCronExpression,
        trigger: trigger,
    });
};

export const forceRunTask = (data: IForceRunRequestData) => {
    const { instanceId, trigger } = data;

    return apiFetch.post(`scheduled-tasks/${instanceId}/execute`, {
        trigger: trigger,
    });
};

export const changeScheduledTaskInterval = (data: IChangeScheduledTaskIntervalRequestData) => {
    const { instanceId, interval, trigger } = data;

    return apiFetch.post(`scheduled-tasks/${instanceId}/modify/interval`, {
        interval: interval,
        trigger: trigger,
    });
};

export const checkCronExpressionValidation = (cronExpression: string) => {
    return apiFetch.post(`scheduled-tasks/validate-cron-expression`, {
        cronExpression: cronExpression,
    });
};
