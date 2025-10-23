import type { ILogger } from "models";

export const filterLoggers = (loggers: ILogger[], search: string): ILogger[] => {
    const formattedSearch = search.toLowerCase().trim();

    return loggers.filter(({ name }) => name.toLowerCase().includes(formattedSearch));
};
