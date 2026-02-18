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
package com.axelixlabs.axelix.common.api.integrations.http;

/**
 * Version of an HTTP protocol in use.
 *
 * @since 05.07.25
 * @author Mikhail Polivakha
 */
public enum HttpVersion {
    V1_0("HTTP 1/0"),
    V1_1("HTTP 1/1"),
    V2_0("HTTP 2/0"),
    V3_0("HTTP 3/0"),
    ;

    private final String display;

    HttpVersion(String display) {
        this.display = display;
    }

    public String getDisplay() {
        return display;
    }
}
