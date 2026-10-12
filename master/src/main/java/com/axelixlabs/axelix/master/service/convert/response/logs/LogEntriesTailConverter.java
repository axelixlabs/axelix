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
package com.axelixlabs.axelix.master.service.convert.response.logs;

import java.time.Instant;

import org.jspecify.annotations.NonNull;

import org.springframework.stereotype.Service;

import com.axelixlabs.axelix.master.contract.logs.LogEntriesTail;
import com.axelixlabs.axelix.master.contract.logs.LogEntry;
import com.axelixlabs.axelix.master.service.convert.response.Converter;

/**
 * The {@link Converter} from {@link LogEntriesTail} to the plain text the AI Agents consume: one line per
 * log entry, in the console-like format, with the stack trace (if any) on the lines that follow, e.g.
 * <pre>
 * 2026-10-12T10:15:30.123Z ERROR [http-nio-8080-exec-1] com.example.OrderService - Payment failed
 * java.lang.IllegalStateException: boom
 *     at com.example.OrderService.pay(OrderService.java:42)
 * </pre>
 *
 * @author Mikhail Polivakha
 */
@Service
public class LogEntriesTailConverter implements Converter<LogEntriesTail, String> {

    @Override
    public @NonNull String convertInternal(@NonNull LogEntriesTail source) {
        StringBuilder result = new StringBuilder();

        for (LogEntry entry : source.getEntries()) {
            result.append(Instant.ofEpochMilli(entry.getTimestamp()))
                    .append(' ')
                    .append(entry.getLevel())
                    .append(" [")
                    .append(entry.getThread())
                    .append("] ")
                    .append(entry.getLogger())
                    .append(" - ")
                    .append(entry.getMessage())
                    .append('\n');

            if (entry.getStackTrace() != null) {
                result.append(entry.getStackTrace().stripTrailing()).append('\n');
            }
        }

        return result.toString();
    }
}
