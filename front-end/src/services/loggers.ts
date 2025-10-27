import apiFetch from "api/apiFetch";
import type { ISetLoggerLevelRequestData } from "models";

export const getLoggersData = (instanceId: string) => {
    return apiFetch.get(`loggers/${instanceId}`);
};

export const setLoggerLevel = (data: ISetLoggerLevelRequestData) => {
    const { instanceId, loggerName, loggingLevel } = data;

    return apiFetch.post(`loggers/${instanceId}/logger/${loggerName}`, {
        configuredLevel: loggingLevel,
    });
};
