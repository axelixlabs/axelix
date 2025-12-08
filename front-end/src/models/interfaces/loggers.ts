/*
 * Copyright 2025-present, Nucleon Forge Software.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
