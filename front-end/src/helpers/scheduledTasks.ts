import type { IScheduledTaskItem } from "models";

export const filterScheduledTasks = (
    scheduledTasksTypes: IScheduledTaskItem[],
    search: string
): IScheduledTaskItem[] => {
    const formattedSearch = search.toLowerCase().trim();

    return scheduledTasksTypes.filter(({ tasks }) => (
        tasks.some(({ runnable }) =>
            runnable.target.toLowerCase().includes(formattedSearch)
        )
    ));
};
