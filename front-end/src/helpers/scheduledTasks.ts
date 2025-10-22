import type { ScheduledTasksResponse } from "models";

export const filterScheduledTasks = (
    scheduledTasksResponse: ScheduledTasksResponse,
    search: string,
): ScheduledTasksResponse => {
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
