import apiFetch from "api/apiFetch";

export const getLoggersData = (id: string) => {
    return apiFetch.get(`loggers/${id}`);
};

export const setLoggerLevel = (instanceId: string, loggerName: string, loggingLevel: string) => {
    return apiFetch.post(`loggers/${instanceId}/logger/${loggerName}`, {
        configuredLevel: loggingLevel
    });
}