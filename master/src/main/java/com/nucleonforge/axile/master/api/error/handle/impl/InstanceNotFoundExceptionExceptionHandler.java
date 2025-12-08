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
package com.nucleonforge.axile.master.api.error.handle.impl;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import com.nucleonforge.axile.master.api.error.ApiError;
import com.nucleonforge.axile.master.api.error.SimpleApiError;
import com.nucleonforge.axile.master.api.error.handle.ExceptionHandler;
import com.nucleonforge.axile.master.exception.InstanceNotFoundException;

/**
 * {@link ExceptionHandler} for {@link InstanceNotFoundException}.
 *
 * @author Mikhail Polivakha
 */
@Component
public class InstanceNotFoundExceptionExceptionHandler implements ExceptionHandler<InstanceNotFoundException> {

    @Override
    public ResponseEntity<ApiError> handle(InstanceNotFoundException exception) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new SimpleApiError("INSTANCE_NOT_FOUND"));
    }

    @Override
    public Class<InstanceNotFoundException> supported() {
        return InstanceNotFoundException.class;
    }
}
