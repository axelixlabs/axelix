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
package com.axelixlabs.axelix.sbs.spring.core.auth;

/**
 * Interface for matching request paths against endpoint URL patterns.
 *
 * @author Sergey Cherkasov
 * @author Mikhail Polivakha
 */
public interface EndpointPathMatcher {

    /**
     * Determines whether the given request path matches the specified endpoint pattern.
     *
     * @param endpointPattern the URL pattern defined for an endpoint
     *                        (e.g., {@code "/actuator/env/**"})
     *
     * @param requestPath     the relative incoming request path to evaluate
     *                        (e.g., {@code "/axelix-beans"}). Note that "relative"
     *                        in the current context means the path that has stripped
     *                        actuator prefix (in most cases the actuator prefix is
     *                        {@code /actuator}, but not always).
     *
     * @return {@code true}   if the endpoint pattern matches the request path, {@code false} otherwise.
     */
    boolean matches(String endpointPattern, String requestPath);
}
