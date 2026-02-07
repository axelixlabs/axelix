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
package com.axelixlabs.axelix.common.auth.core;

/**
 * The authority that is used internally, i.e. the requests from the browser
 * that runs the front-end javascript are not supposed to have any of those
 * authorities ever. These authorities are just for internal Axelix Master -->
 * Axelix Managed Service (the one that includes the starter) communication.
 *
 * @author Mikhail Polivakha
 */
public class InternalAuthorities {

    /**
     * The authority that allows the potential Spring Boot service candidate to register itslef inside Axelix Master.
     */
    public static final InternalAuthority SELF_REGISTER_AUTHORITY = new InternalAuthority("INTERNAL.SELF_REGISTER");

    public static class InternalAuthority implements Authority {

        private final String authorityName;

        public InternalAuthority(String authorityName) {
            this.authorityName = authorityName;
        }

        @Override
        public String getName() {
            return this.authorityName;
        }
    }
}
