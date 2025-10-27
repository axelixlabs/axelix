import type { ScheduledTasksResponseBody } from "models";

export const filterScheduledTasks = (
    scheduledTasksResponse: ScheduledTasksResponseBody,
    search: string,
): ScheduledTasksResponseBody => {
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

export function isEmpty(resp: ScheduledTasksResponseBody): boolean {
    return resp.cron.length === 0 && resp.fixedDelay.length === 0 && resp.fixedRate.length === 0;
}
