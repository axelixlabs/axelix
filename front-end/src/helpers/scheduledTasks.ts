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
