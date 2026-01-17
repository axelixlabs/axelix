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
package com.nucleonforge.axelix.master.api.error.handle;

import com.nucleonforge.axelix.master.api.error.ApiError;

/**
 * Common interface for any classes that are capable to translate the given {@link Exception} to
 * concrete abstraction over the Http Response, i.e. {@link org.springframework.http.ResponseEntity}.
 *
 * @author Mikhail Polivakha
 */
public interface ApiExceptionTranslator {

    /**
     * Translate given Exception into an {@link ApiError}.
     *
     * @param e exception to translate. Cannot be {@code null}.
     * @return the ApiError which is a representation of an occurred exception for the API layer.
     */
    ApiError translateException(Exception e);
}
