import apiFetch from "api/apiFetch";
import type { IUpdateScheduledTasksRequestData } from "models";

export const getScheduledTasksData = (instanceId: string) => {
    return apiFetch.get(`/scheduled-tasks/${instanceId}`);
};

export const updateScheduledTasksStatus = (data: IUpdateScheduledTasksRequestData) => {
    const { instanceId, statusType, targetScheduledTask, force } = data;

    return apiFetch.post(`/scheduled-tasks/${instanceId}/${statusType}`, {
        targetScheduledTask: targetScheduledTask,
        force: force,
    });
};
