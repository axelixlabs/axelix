import apiFetch from "api/apiFetch";

export const getScheduledTasksData = (instanceId: string) => {
    return apiFetch.get(`/scheduled-tasks/${instanceId}`);
};