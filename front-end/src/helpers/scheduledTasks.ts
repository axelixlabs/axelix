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

import type { IScheduledTasksResponseBody } from "models";

export const filterScheduledTasks = (
    scheduledTasksResponse: IScheduledTasksResponseBody,
    search: string,
): IScheduledTasksResponseBody => {
    const formattedSearch = search.toLowerCase().trim();

    return {
        cron: scheduledTasksResponse.cron.filter((value) =>
            value.runnable.target.toLowerCase().includes(formattedSearch),
        ),

        fixedDelay: scheduledTasksResponse.fixedDelay.filter((value) =>
            value.runnable.target.toLowerCase().includes(formattedSearch),
        ),

        fixedRate: scheduledTasksResponse.fixedRate.filter((value) =>
            value.runnable.target.toLowerCase().includes(formattedSearch),
        ),
    };
};

export function isEmpty(resp: IScheduledTasksResponseBody): boolean {
    return resp.cron.length === 0 && resp.fixedDelay.length === 0 && resp.fixedRate.length === 0;
}

export const getCronDescription = (cron: string): string => {
    try {
        return cronstrue.toString(cron);
    } catch {
        return "Invalid cron expression";
    }
};
