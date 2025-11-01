import type { ILogger, ILoggerGroup } from "models";

export const filterLoggers = (loggers: ILogger[], search: string): ILogger[] => {
    const formattedSearch = search.toLowerCase().trim();

    return loggers.filter(({ name }) => name.toLowerCase().includes(formattedSearch));
};

export const filterLoggerGroupsOrLoggers = (loggerGroups: ILoggerGroup[], search: string): ILoggerGroup[] => {
    const formattedSearch = search.toLowerCase().trim();

    return loggerGroups.reduce<ILoggerGroup[]>((result, loggerGroup) => {
        const { name, members, configuredLevel } = loggerGroup;

        const loggerGroupNameLower = name.toLowerCase();

        if (loggerGroupNameLower.includes(formattedSearch)) {
            result.push(loggerGroup);
            return result;
        }

        const filteredMembers = members.filter((member) => member.toLowerCase().includes(formattedSearch));

        if (filteredMembers.length) {
            result.push({
                name: name,
                configuredLevel: configuredLevel,
                members: filteredMembers,
            });
        }
        return result;
    }, []);
};

export const getLoggerGroupsLoggersCount = (loggerGroups: ILoggerGroup[]): number => {
    return loggerGroups.reduce((result, { members }) => result + members.length, 0);
};
