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
package com.axelixlabs.axelix.sbs.spring.core.integration;

import java.util.Objects;

/**
 * Abstraction over tcp socket.
 *
 * @since 08.07.25
 * @author Mikhail Polivakha
 */
public class TCPSocket {

    private final String host;
    private final int port;

    /**
     * Create new TCPSocket
     *
     * @param host peer connection host
     * @param port peer connection port
     *
     * @since 08.07.25
     * @author Mikhail Polivakha
     */
    public TCPSocket(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public String getHost() {
        return host;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        TCPSocket tcpSocket = (TCPSocket) o;
        return port == tcpSocket.port && Objects.equals(host, tcpSocket.host);
    }

    @Override
    public int hashCode() {
        return Objects.hash(host, port);
    }

    @Override
    public String toString() {
        return host + ":" + port;
    }
}
