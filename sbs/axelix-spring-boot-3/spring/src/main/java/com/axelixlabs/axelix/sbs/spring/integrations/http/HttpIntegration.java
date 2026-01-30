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
package com.axelixlabs.axelix.sbs.spring.integrations.http;

import com.axelixlabs.axelix.sbs.spring.integrations.AbstractIntegration;

/**
 * Represents an HTTP-based integration with an external service.
 *
 * @since 05.07.2025
 * @author Mikhail Polivakha
 */
public final class HttpIntegration extends AbstractIntegration {

    public HttpIntegration(String networkAddress, HttpVersion httpVersion) {
        this(networkAddress, httpVersion, "External HTTP Service");
    }

    public HttpIntegration(String networkAddress, HttpVersion httpVersion, String serviceName) {
        super(networkAddress, httpVersion.getDisplay(), serviceName);
    }
}
