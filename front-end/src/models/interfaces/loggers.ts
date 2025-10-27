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

export interface ILoggersResponseBody {
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
