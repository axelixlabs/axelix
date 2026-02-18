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
package com.axelixlabs.axelix.common.api.integrations;

import java.util.HashMap;
import java.util.Map;

/**
 * The integration that service has with some other entity on the network
 *
 * @since 05.07.25
 * @author Mikhail Polivakha
 */
public interface Integration {

    /**
     * @return abstract term that defines the type of entity with which the integration takes place
     */
    String entityType();

    /**
     * Protocol being used for communication
     */
    String protocol();

    /**
     * @return network address being used inside the app for communicating with this entity
     */
    String networkAddress();

    /**
     * @return key-value pairs, that represent some properties, that are specific to this integration or integration entity
     */
    default Map<String, Object> properties() {
        return new HashMap<>(0);
    }
}
