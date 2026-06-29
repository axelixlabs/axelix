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
package com.axelixlabs.axelix.sbs.spring.core.transactions.hibernate;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.Property;

/**
 * Log4j2 appender that detects Hibernate's in-memory pagination by intercepting
 * the {@code HHH90003004} warning emitted by Hibernate 6.x–7.3.x when {@code firstResult/maxResults}
 * is specified with a collection fetch.
 *
 * <p>Note: starting from Hibernate 7.4, in-memory pagination is no longer applied,
 * so this appender will never trigger on Hibernate 7.4 and above.
 *
 * @author Vyacheslav Yanin
 * @see LogbackInMemoryPaginationAppender
 */
public class Log4j2InMemoryPaginationAppender extends AbstractAppender {

    // Only for Hibernate 6.x – 7.3.x (Spring Boot 3.x and 4.0.x).
    private static final String HHH90003004 = "HHH90003004";

    protected Log4j2InMemoryPaginationAppender(String name) {
        super(name, null, null, true, Property.EMPTY_ARRAY);
    }

    @Override
    public void append(LogEvent event) {
        if (event.getLevel() != Level.WARN) {
            return;
        }
        String message = event.getMessage().getFormattedMessage();
        if (message != null && (message.contains(HHH90003004))) {
            InMemoryPaginationHolder.mark();
        }
    }
}
