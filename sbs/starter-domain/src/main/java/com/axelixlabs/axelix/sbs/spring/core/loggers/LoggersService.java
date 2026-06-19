/*
 * Copyright (C) 2025-2026 Axelix Labs
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package com.axelixlabs.axelix.sbs.spring.core.loggers;

import com.axelixlabs.axelix.common.api.loggers.LogLevelChangeRequest;
import com.axelixlabs.axelix.common.api.loggers.LoggersFeed;
import com.axelixlabs.axelix.common.api.loggers.LoggersGroupProfile;
import com.axelixlabs.axelix.common.api.loggers.SingleLoggerProfile;
import com.axelixlabs.axelix.sbs.spring.core.loggers.exceptions.LogLevelNotFoundException;
import com.axelixlabs.axelix.sbs.spring.core.loggers.exceptions.LoggerNotFoundException;

/**
 * Service for managing Loggers.
 *
 * @author Nikita Kirillov
 */
public interface LoggersService {

    /**
     * @return the feed of loggers that exists in this application
     */
    LoggersFeed getAllLoggers();

    /**
     * @param loggerName the name of the logger to retrieve.
     * @return the profile of a single logger.
     * @throws LoggerNotFoundException in case the logger with such name does not exist.
     */
    SingleLoggerProfile getSingleLogger(String loggerName) throws LoggerNotFoundException;

    /**
     * @param groupName the name of the logger group to retrieve.
     * @return the profile of a logger group with the given group name.
     * @throws LoggerNotFoundException in case the logger group with such name does not exist.
     */
    LoggersGroupProfile getLoggerGroup(String groupName) throws LoggerNotFoundException;

    /**
     * @param loggerName the name of the logger to whose logging level to change at runtime.
     * @param changeRequest the request that represents the logging level change.
     *
     * @throws LoggerNotFoundException in case the logger with the given name cannot be found.
     * @throws LogLevelNotFoundException in case the log level specified in request cannot be found.
     */
    void changeLogLevelByLoggerName(String loggerName, LogLevelChangeRequest changeRequest)
            throws LoggerNotFoundException, LogLevelNotFoundException;

    /**
     * @param groupName the name of the logger group to whose logging level to change at runtime.
     * @param changeRequest the request that represents the logging level change.
     *
     * @throws LoggerNotFoundException in case the logger group with the given name cannot be found.
     * @throws LogLevelNotFoundException in case the log level specified in request cannot be found.
     */
    void changeLogLevelByGroupName(String groupName, LogLevelChangeRequest changeRequest)
            throws LoggerNotFoundException, LogLevelNotFoundException;

    /**
     * Reset the logging level of the logger to the level that it is now supposed to be in. In other words,
     * resetting means clearing the configured level (or resetting to the original, that was prior to any
     * modifications from Axelix side) of this exact logger.
     *
     * @param loggerName the name of the logger to reset.
     *
     * @throws LoggerNotFoundException in case the logger with the given name cannot be found.
     * @throws LogLevelNotFoundException in case the log level specified in request cannot be found.
     */
    void resetLogLevelByLoggerName(String loggerName) throws LoggerNotFoundException, LogLevelNotFoundException;
}
