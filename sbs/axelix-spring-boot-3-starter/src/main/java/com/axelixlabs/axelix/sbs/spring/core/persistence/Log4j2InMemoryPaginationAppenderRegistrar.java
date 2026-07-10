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
package com.axelixlabs.axelix.sbs.spring.core.persistence;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.slf4j.Log4jLoggerFactory;
import org.slf4j.ILoggerFactory;
import org.slf4j.LoggerFactory;

/**
 * Log4j2 implementation of {@link InMemoryPaginationAppenderRegistrar}.
 *
 * @author Vyacheslav Yanin
 */
class Log4j2InMemoryPaginationAppenderRegistrar implements InMemoryPaginationAppenderRegistrar {

    private static final String APPENDER_NAME = "axelix-in-memory-pagination";
    // Hibernate 6+ (Spring Boot 3)
    private static final String HIBERNATE_LOGGER = "org.hibernate.orm.query";

    @Override
    public void register() {
        ILoggerFactory factory = LoggerFactory.getILoggerFactory();
        if (!(factory instanceof Log4jLoggerFactory)) {
            return;
        }

        if (!(LogManager.getContext(false) instanceof LoggerContext context)) {
            return;
        }

        Configuration configuration = context.getConfiguration();
        Logger logger = context.getLogger(HIBERNATE_LOGGER);
        if (logger.getAppenders().get(APPENDER_NAME) != null) {
            return;
        }

        Log4j2InMemoryPaginationAppender appender = new Log4j2InMemoryPaginationAppender(APPENDER_NAME);
        appender.start();
        configuration.addAppender(appender);
        logger.addAppender(appender);

        context.updateLoggers();
    }
}
