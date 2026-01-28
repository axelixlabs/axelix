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
package com.axelixlabs.axelix.master.api.error;

import org.jspecify.annotations.NonNull;

/**
 * Interface for an error to be sent from the master backend to the front-end app.
 *
 * @author Mikhail Polivakha
 */
public interface ApiError {

    /**
     * @return Code of the error. Guaranteed to be not null.
     */
    @NonNull
    String errorCode();

    /**
     * @return the HTTP status code to return.
     */
    int statusCode();

    //
    //    /**
    //     * Any possible additional parameters that may communicate some context about
    //     * the error that happened. This {@link Map} cannot be null, but it can easily
    //     * be empty in case backend does not consider to send any additional parameters.
    //     * This {@link Map} may contain some internal complex structures, such as Other
    //     * {@link Map maps} for intance.
    //     */
    //    @NonNull Map<String, Object> params();
}
