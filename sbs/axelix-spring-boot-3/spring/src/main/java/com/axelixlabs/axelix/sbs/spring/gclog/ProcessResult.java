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
package com.axelixlabs.axelix.sbs.spring.gclog;

/**
 * Represents the result of an external process execution.
 *
 * @since 29.12.2025
 * @author Nikita Kirillov
 */
public class ProcessResult {
    private final boolean success;
    private final String output;

    public ProcessResult(int exitCode, String output) {
        this.success = exitCode == 0;
        this.output = output;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getOutput() {
        return output;
    }
}
