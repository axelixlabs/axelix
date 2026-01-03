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
package com.nucleonforge.axelix.master.api.error.handle.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.http.HttpStatus;

import com.nucleonforge.axelix.master.api.error.ApiError;
import com.nucleonforge.axelix.master.api.error.SimpleApiError;
import com.nucleonforge.axelix.master.api.error.handle.ApiErrorCodes;
import com.nucleonforge.axelix.master.api.error.handle.ExceptionHandler;

/**
 * The default {@link ExceptionHandler} where calls are forwarded when no specific
 * {@link ExceptionHandler ExceptionHandlers} are found.
 *
 * @author Mikhail Polivakha
 */
public class DefaultExceptionHandler implements ExceptionHandler<Exception> {

    public static final DefaultExceptionHandler INSTANCE = new DefaultExceptionHandler();
    private static final Logger log = LoggerFactory.getLogger(DefaultExceptionHandler.class);

    @Override
    public ApiError handle(Exception exception) {
        log.warn("Default exception handler received an exception", exception);
        return new SimpleApiError(
                ApiErrorCodes.INTERNAL_SERVER_ERROR_CODE.getErrorCode(), HttpStatus.INTERNAL_SERVER_ERROR.value());
    }

    @Override
    public Class<Exception> supported() {
        return Exception.class;
    }
}
