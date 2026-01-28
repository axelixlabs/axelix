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
package com.axelixlabs.axelix.sbs.spring.logfile;

import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.actuate.logging.LogFileWebEndpoint;
import org.springframework.core.io.Resource;

/**
 * Custom Spring Boot Actuator endpoint exposing the application's log file.
 *
 * @author Sergey Cherkasov
 */
@Endpoint(id = "axelix-log-file")
public class AxelixLogFileEndpoint {

    private final LogFileWebEndpoint delegate;

    public AxelixLogFileEndpoint(LogFileWebEndpoint delegate) {
        this.delegate = delegate;
    }

    @ReadOperation(produces = "text/plain; charset=UTF-8")
    public Resource getLogFile() {

        return delegate.logFile();
    }
}
