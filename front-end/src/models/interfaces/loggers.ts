export interface ILogger {
    /**
     * Logger name
     */
    name: string;
    /**
     * Explicitly configured level for logger, if any
     */
    configuredLevel?: string;
    /**
     * Single logger current level
     */
    effectiveLevel: string;
}

export interface ILoggerGroup {
    /**
     * The name of a logger group
     */
    name: string;
    /**
     * The configured level of a logger group
     */
    configuredLevel?: string;
    /**
     * Members of a logger group
     */
    members: string[];
}

export interface ILoggersResponseBody {
    /**
     * All logger groups data
     */
    groups: ILoggerGroup[];
    /**
     * All possible logging levels that are supported by the logging system inside the instance
     */
    levels: string[];
    /**
     * All loggers
     */
    loggers: ILogger[];
}

export interface ISetLoggerLevelRequestData {
    /**
     * Instance id
     */
    instanceId: string;
    /**
     * Logger name
     */
    loggerName: string;
    /**
     * Selected level
     */
    loggingLevel: string;
}

export interface IChangeLoggerGroupLevelRequestData {
    /**
     * Instance id of service
     */
    instanceId: string;
    /**
     * The name of a logger group
     */
    groupName: string;
    /**
     * The configured level of a logger group
     */
    configuredLevel: string;
}
