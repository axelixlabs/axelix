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

import com.nucleonforge.axelix.common.auth.exception.JwtProcessingException;
import com.nucleonforge.axelix.master.api.error.ApiError;
import com.nucleonforge.axelix.master.api.error.SimpleApiError;
import com.nucleonforge.axelix.master.api.error.handle.ApiErrorCodes;
import com.nucleonforge.axelix.master.api.error.handle.ExceptionHandler;

/**
 * {@link ExceptionHandler} for {@link JwtProcessingException}.
 *
 * @author Mikhail Polivakha
 */
@Component
public class JwtProcessingExceptionHandler implements ExceptionHandler<JwtProcessingException> {

    @Override
    public ApiError handle(JwtProcessingException exception) {
        return new SimpleApiError(
                ApiErrorCodes.INVALID_JWT_EXCEPTION_CODE.getErrorCode(), HttpStatus.UNAUTHORIZED.value());
    }

    @Override
    public Class<JwtProcessingException> supported() {
        return JwtProcessingException.class;
    }
}
