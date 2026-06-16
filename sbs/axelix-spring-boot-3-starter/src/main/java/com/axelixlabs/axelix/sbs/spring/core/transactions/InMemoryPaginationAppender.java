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

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;

/**
 * Logback appender that detects Hibernate's in-memory pagination by intercepting
 * the {@code HHH90003004} warning emitted by Hibernate 6.x–7.3.x when {@code firstResult/maxResults}
 * is specified with a collection fetch.
 *
 * <p>Note: starting from Hibernate 7.4, in-memory pagination is no longer applied,
 * so this appender will never trigger on Hibernate 7.4 and above.
 *
 * @author Nikita Kirillov
 */
public class InMemoryPaginationAppender extends AppenderBase<ILoggingEvent> {

    // Only for Hibernate 6.x – 7.3.x (Spring Boot 3.x and 4.0.x).
    private static final String HHH90003004 = "HHH90003004";

    @Override
    protected void append(ILoggingEvent event) {
        if (event.getLevel() != Level.WARN) {
            return;
        }
        String message = event.getMessage();
        if (message != null && (message.contains(HHH90003004))) {
            InMemoryPaginationHolder.mark();
        }
    }
}
