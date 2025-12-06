import type { ILogger, ILoggerGroup, ILoggersSearchFilters } from "models";

export const filterLoggersByLoggerName = (loggers: ILogger[], search: string): ILogger[] => {
    const formattedSearch = search.toLowerCase().trim();

    return loggers.filter(({ name }) => name.toLowerCase().includes(formattedSearch));
};

export const filterLoggerGroups = (loggerGroups: ILoggerGroup[], search: string): ILoggerGroup[] => {
    const formattedSearch = search.toLowerCase().trim();

    return loggerGroups.reduce<ILoggerGroup[]>((result, loggerGroup) => {
        const { name, members } = loggerGroup;

        const loggerGroupNameLower = name.toLowerCase();

        if (loggerGroupNameLower.includes(formattedSearch)) {
            result.push(loggerGroup);
            return result;
        }

        const anyLoggerMatches = members.some((member) => member.toLowerCase().includes(formattedSearch));

        if (anyLoggerMatches) {
            result.push(loggerGroup);
            return result;
        }
        return result;
    }, []);
};

const isClassLogger = (name: string): boolean => {
    const last = name.trim().split(".").pop();
    if (!last) {
        return false;
    }

    return /^[A-Z][A-Za-z0-9_$]*$/.test(last);
};

export const filterLoggers = (loggers: ILogger[], searchFilters: ILoggersSearchFilters) => {
    let filteredLoggers = loggers;

    if (searchFilters.isConfiguredSearch) {
        filteredLoggers = filteredLoggers.filter((logger) => logger.configuredLevel);
    }

    if (searchFilters.isClassOnlySearch) {
        filteredLoggers = filteredLoggers.filter((logger) => isClassLogger(logger.name));
    }

    return filteredLoggers;
};
