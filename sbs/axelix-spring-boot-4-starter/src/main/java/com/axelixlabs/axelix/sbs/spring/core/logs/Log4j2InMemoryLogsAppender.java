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

import java.io.PrintWriter;
import java.io.StringWriter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.Property;

import com.axelixlabs.axelix.sbs.spring.core.SlidingWindow;
import com.axelixlabs.axelix.sbs.spring.core.contract.logs.LogEntry;

/**
 * Log4j2 appender that keeps the most recent log entries of the application in memory.
 *
 * @author Mikhail Polivakha
 * @see LogbackInMemoryLogsAppender
 */
public class Log4j2InMemoryLogsAppender extends AbstractAppender {

    private static final String APPENDER_NAME = "axelix-in-memory-logs";

    private final SlidingWindow<LogEntry> logEntries;

    protected Log4j2InMemoryLogsAppender(SlidingWindow<LogEntry> logEntries) {
        super(APPENDER_NAME, null, null, true, Property.EMPTY_ARRAY);
        this.logEntries = logEntries;
    }

    /**
     * Attaches the appender to the root logger, replacing the one possibly left there by the
     * previous application context. If Log4j2 is not the active logging backend, this method
     * returns silently.
     */
    public static void register(SlidingWindow<LogEntry> logEntries) {
        if (!(LogManager.getContext(false) instanceof LoggerContext)) {
            return;
        }

        LoggerContext context = (LoggerContext) LogManager.getContext(false);
        Logger logger = context.getRootLogger();
        Appender previous = logger.getAppenders().get(APPENDER_NAME);
        if (previous != null) {
            logger.removeAppender(previous);
        }

        Log4j2InMemoryLogsAppender appender = new Log4j2InMemoryLogsAppender(logEntries);
        appender.start();
        context.getConfiguration().addAppender(appender);
        logger.addAppender(appender);

        context.updateLoggers();
    }

    @Override
    public void append(LogEvent event) {
        LogEntry entry = new LogEntry()
                .timestamp(event.getTimeMillis())
                .level(event.getLevel().name())
                .logger(event.getLoggerName())
                .thread(event.getThreadName())
                .message(event.getMessage().getFormattedMessage());

        if (event.getThrown() != null) {
            StringWriter stackTrace = new StringWriter();
            event.getThrown().printStackTrace(new PrintWriter(stackTrace));
            entry.stackTrace(stackTrace.toString());
        }

        logEntries.put(entry);
    }
}
