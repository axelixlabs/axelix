/*
 * Copyright 2025-present, Nucleon Forge Software.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
