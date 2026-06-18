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

    LoggersFeed getAllLoggers();

    SingleLoggerProfile getSingleLogger(String loggerName) throws LoggerNotFoundException;

    LoggersGroupProfile getLoggerGroup(String groupName) throws LoggerNotFoundException;

    void changeLogLevelByLoggerName(String loggerName, LogLevelChangeRequest changeRequest)
            throws LoggerNotFoundException, LogLevelNotFoundException;

    void changeLogLevelByGroupName(String groupName, LogLevelChangeRequest changeRequest)
            throws LoggerNotFoundException, LogLevelNotFoundException;

    void resetLogLevelByLoggerName(String loggerName) throws LoggerNotFoundException, LogLevelNotFoundException;
}
