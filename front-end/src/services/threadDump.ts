import apiFetch from "api/apiFetch";

export const getThreadDumpData = (instanceId: string) => {
    return apiFetch.get(`/thread-dump/${instanceId}`);
};
