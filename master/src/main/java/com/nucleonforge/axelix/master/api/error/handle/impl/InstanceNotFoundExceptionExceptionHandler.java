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
import com.nucleonforge.axelix.master.api.error.handle.ExceptionHandler;
import com.nucleonforge.axelix.master.exception.InstanceNotFoundException;

/**
 * {@link ExceptionHandler} for {@link InstanceNotFoundException}.
 *
 * @author Mikhail Polivakha
 */
@Component
public class InstanceNotFoundExceptionExceptionHandler implements ExceptionHandler<InstanceNotFoundException> {

    private static final String INSTANCE_NOT_FOUND_CODE = "INSTANCE_NOT_FOUND";

    @Override
    public ApiError handle(InstanceNotFoundException exception) {
        return new SimpleApiError(INSTANCE_NOT_FOUND_CODE, HttpStatus.BAD_REQUEST.value());
    }

    @Override
    public Class<InstanceNotFoundException> supported() {
        return InstanceNotFoundException.class;
    }
}
