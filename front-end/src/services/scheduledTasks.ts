import apiFetch from "api/apiFetch";
import type { IUpdateScheduledTasksBody } from "models";

export const getScheduledTasksData = (instanceId: string) => {
    return apiFetch.get(`/scheduled-tasks/${instanceId}`);
};

export const updateScheduledTasksStatus = (responseBody: IUpdateScheduledTasksBody) => {
    const { instanceId, statusType, targetScheduledTask, force } = responseBody
    return apiFetch.post(`/scheduled-tasks/${instanceId}/${statusType}`, {
        targetScheduledTask: targetScheduledTask,
        force: force
    });
}