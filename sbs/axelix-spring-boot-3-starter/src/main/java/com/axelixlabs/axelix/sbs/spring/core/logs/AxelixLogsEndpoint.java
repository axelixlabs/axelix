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

import java.util.List;

import org.springframework.boot.actuate.endpoint.web.annotation.RestControllerEndpoint;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;

import com.axelixlabs.axelix.common.contract.logs.LogEntriesTail;
import com.axelixlabs.axelix.common.contract.logs.LogEntry;
import com.axelixlabs.axelix.sbs.spring.core.SlidingWindow;

/**
 * Custom Actuator endpoint that returns the most recent log entries of the application.
 *
 * @author Mikhail Polivakha
 */
@RestControllerEndpoint(id = "axelix-logs")
public class AxelixLogsEndpoint {

    private final SlidingWindow<LogEntry> logEntries;

    public AxelixLogsEndpoint(SlidingWindow<LogEntry> logEntries) {
        this.logEntries = logEntries;
    }

    @GetMapping
    public LogEntriesTail tail(@RequestParam(required = false) Integer limit) {
        List<LogEntry> entries = logEntries.get();

        if (limit == null) {
            return new LogEntriesTail().entries(entries);
        }

        if (limit < 1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The limit must be at least 1");
        }

        int from = Math.max(0, entries.size() - limit);
        return new LogEntriesTail().entries(entries.subList(from, entries.size()));
    }
}
