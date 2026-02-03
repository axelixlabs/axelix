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
package com.axelixlabs.axelix.master.internal;

/**
 * Constant class containing API paths for interaction between Axelix Master and Starter.
 *
 * @author Sergey Cherkasov
 */
public final class InternalApiPaths {

    private InternalApiPaths() {}

    public static final class SelfRegistryApi {

        /**
         * Base path for all beans APIs.
         */
        public static final String MAIN = "/service";

        /**
         * Endpoint for service self-registration.
         */
        public static final String SERVICE_REGISTER = "/register";
    }
}
