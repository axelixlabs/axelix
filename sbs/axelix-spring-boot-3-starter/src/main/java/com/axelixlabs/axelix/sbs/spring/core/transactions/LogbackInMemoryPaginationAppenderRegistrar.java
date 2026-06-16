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
package com.axelixlabs.axelix.sbs.spring.core.transactions;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import org.slf4j.ILoggerFactory;
import org.slf4j.LoggerFactory;

/**
 * Logback implementation of {@link InMemoryPaginationAppenderRegistrar}.
 *
 * @author Nikita Kirillov
 */
public class LogbackInMemoryPaginationAppenderRegistrar implements InMemoryPaginationAppenderRegistrar {

    private static final String APPENDER_NAME = "axelix-in-memory-pagination";
    // Hibernate 6+ (Spring Boot 3)
    private static final String HIBERNATE_LOGGER = "org.hibernate.orm.query";

    @Override
    public void register() {
        ILoggerFactory factory = LoggerFactory.getILoggerFactory();
        if (!(factory instanceof LoggerContext context)) {
            return;
        }

        Logger logger = context.getLogger(HIBERNATE_LOGGER);
        if (logger.getAppender(APPENDER_NAME) != null) {
            return;
        }

        LogbackInMemoryPaginationAppender appender = new LogbackInMemoryPaginationAppender();
        appender.setName(APPENDER_NAME);
        appender.setContext(context);
        appender.start();

        logger.addAppender(appender);
    }
}
