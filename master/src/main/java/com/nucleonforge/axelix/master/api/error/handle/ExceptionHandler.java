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
 * Implementations are capable to handle a very particular {@link Exception}
 * in the exception hierarchy. By "handling" in this context we mean the
 * act of translation {@link Exception} to an {@link ApiError}.
 *
 * @author Mikhail Polivakha
 */
public interface ExceptionHandler<T extends Exception> {

    /**
     * Handle/Translate the exception to {@link ApiError}.
     *
     * @param exception to handle
     * @return {@link ApiError} resulting from incoming exception.
     */
    ApiError handle(T exception);

    /**
     * @return the class of the exception for processing of
     * which this {@link ExceptionHandler} is responsible for.
     */
    Class<T> supported();
}
