import type { ILogger, ILoggerGroup } from "models";

export const filterLoggers = (loggers: ILogger[], search: string): ILogger[] => {
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
