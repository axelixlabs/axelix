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

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;

/**
 * Logback appender that detects Hibernate's in-memory pagination by intercepting
 * the {@code HHH000104} warning emitted by Hibernate 5.x when {@code firstResult/maxResults}
 * is specified with a collection fetch.
 *
 * @author Nikita Kirillov
 */
public class InMemoryPaginationAppender extends AppenderBase<ILoggingEvent> {

    // Only for Hibernate 5.x (Spring Boot 2.x).
    private static final String HHH000104 = "HHH000104";

    @Override
    protected void append(ILoggingEvent event) {
        String message = event.getMessage();
        if (message != null && (message.contains(HHH000104))) {
            InMemoryPaginationHolder.mark();
        }
    }
}
