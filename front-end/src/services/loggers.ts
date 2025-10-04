import apiFetch from "api/apiFetch";

export const getLoggersData = (id: string) => {
    return apiFetch.get(`loggers/${id}`);
};

export const setLoggerLevel = (id: string, loggerName: string, loggingLevel: string) => {
    return apiFetch.post(`loggers/${id}/logger/${loggerName}`, {
        configuredLevel: loggingLevel
    });
}