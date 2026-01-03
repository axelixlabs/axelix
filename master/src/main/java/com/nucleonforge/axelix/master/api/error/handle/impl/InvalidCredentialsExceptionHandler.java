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

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import com.nucleonforge.axelix.master.api.error.ApiError;
import com.nucleonforge.axelix.master.api.error.SimpleApiError;
import com.nucleonforge.axelix.master.api.error.handle.ApiErrorCodes;
import com.nucleonforge.axelix.master.api.error.handle.ExceptionHandler;
import com.nucleonforge.axelix.master.exception.auth.InvalidCredentialsException;

/**
 * The exception handler for the {@link InvalidCredentialsException}.
 *
 * @author Mikhail Polivakha
 */
@Component
public class InvalidCredentialsExceptionHandler implements ExceptionHandler<InvalidCredentialsException> {

    @Override
    public ApiError handle(InvalidCredentialsException exception) {
        return new SimpleApiError(
                ApiErrorCodes.INVALID_CREDENTIALS_CODE.getErrorCode(), HttpStatus.UNAUTHORIZED.value());
    }

    @Override
    public Class<InvalidCredentialsException> supported() {
        return InvalidCredentialsException.class;
    }
}
