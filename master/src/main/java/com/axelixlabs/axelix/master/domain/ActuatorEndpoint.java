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
package com.axelixlabs.axelix.master.domain;

import org.jspecify.annotations.NullMarked;

import com.axelixlabs.axelix.common.domain.http.HttpMethod;
import com.axelixlabs.axelix.common.domain.http.HttpUrl;

/**
 * Spring Actuator Endpoint.
 *
 * @param httpMethod the HTTP method by which this actuator endpoint should be reached.
 * @param path the specific path for this actuator endpoint, that follows the {@code /actuator}. For instance, for the
 *      beans endpoint, the path would be {@literal /axelix-beans}
 * @author Mikhail Polivakha
 */
@NullMarked
public record ActuatorEndpoint(HttpUrl path, HttpMethod httpMethod) {

    public static ActuatorEndpoint of(String path, HttpMethod httpMethod) {
        HttpUrl httpUrl = new HttpUrl(path);
        return new ActuatorEndpoint(httpUrl, httpMethod);
    }
}
