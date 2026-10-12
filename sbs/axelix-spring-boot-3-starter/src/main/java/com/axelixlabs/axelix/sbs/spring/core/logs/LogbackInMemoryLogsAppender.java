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
package com.axelixlabs.axelix.sbs.spring.core.logs;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.ThrowableProxyUtil;
import ch.qos.logback.core.AppenderBase;
import org.slf4j.ILoggerFactory;
import org.slf4j.LoggerFactory;

import com.axelixlabs.axelix.sbs.spring.core.SlidingWindow;
import com.axelixlabs.axelix.sbs.spring.core.contract.logs.LogEntry;

/**
 * Logback appender that keeps the most recent log entries of the application in memory.
 *
 * @author Mikhail Polivakha
 * @see Log4j2InMemoryLogsAppender
 */
public class LogbackInMemoryLogsAppender extends AppenderBase<ILoggingEvent> {

    private static final String APPENDER_NAME = "axelix-in-memory-logs";

    private final SlidingWindow<LogEntry> logEntries;

    public LogbackInMemoryLogsAppender(SlidingWindow<LogEntry> logEntries) {
        this.logEntries = logEntries;
    }

    /**
     * Attaches the appender to the root logger, replacing the one possibly left there by the
     * previous application context. If Logback is not the active logging backend, this method
     * returns silently.
     */
    public static void register(SlidingWindow<LogEntry> logEntries) {
        ILoggerFactory factory = LoggerFactory.getILoggerFactory();
        if (!(factory instanceof LoggerContext)) {
            return;
        }

        LoggerContext context = (LoggerContext) factory;
        Logger logger = context.getLogger(Logger.ROOT_LOGGER_NAME);
        logger.detachAppender(APPENDER_NAME);

        LogbackInMemoryLogsAppender appender = new LogbackInMemoryLogsAppender(logEntries);
        appender.setName(APPENDER_NAME);
        appender.setContext(context);
        appender.start();

        logger.addAppender(appender);
    }

    @Override
    protected void append(ILoggingEvent event) {
        LogEntry entry = new LogEntry()
                .timestamp(event.getTimeStamp())
                .level(event.getLevel().toString())
                .logger(event.getLoggerName())
                .thread(event.getThreadName())
                .message(event.getFormattedMessage());

        if (event.getThrowableProxy() != null) {
            entry.stackTrace(ThrowableProxyUtil.asString(event.getThrowableProxy()));
        }

        logEntries.put(entry);
    }
}
