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
package com.axelixlabs.axelix.master.utils;

import org.springframework.test.util.TestSocketUtils;

/**
 * Test utilities for allocating TCP ports.
 *
 * @author Dmitry Mazurov
 */
public class TestPortUtils {

    private TestPortUtils() {}

    /**
     * Finds an available TCP port, retrying until it differs from {@code excludedPort}. Useful
     * when a test needs a port guaranteed not to equal some other fixed port used in the same
     * test (e.g. a well-known default like {@code 8080}).
     *
     * @param excludedPort the port value that must not be returned
     */
    public static int findAvailableTcpPortOtherThan(int excludedPort) {
        int port;
        do {
            port = TestSocketUtils.findAvailableTcpPort();
        } while (port == excludedPort);
        return port;
    }
}
