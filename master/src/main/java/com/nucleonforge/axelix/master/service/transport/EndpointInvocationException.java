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
package com.nucleonforge.axelix.master.service.transport;

import com.nucleonforge.axelix.common.domain.spring.actuator.ActuatorEndpoint;

/**
 * The exception that occurs when Axelix Master tried to reach a particular {@link ActuatorEndpoint}
 * on the managed service, but the managed service is either not available, or responded with non 2xx status
 * code.
 *
 * @author Mikhail Polivakha
 */
public class EndpointInvocationException extends RuntimeException {

    public EndpointInvocationException(Throwable cause) {
        super(cause);
    }

    public EndpointInvocationException(String message) {
        super(message);
    }
}
