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
