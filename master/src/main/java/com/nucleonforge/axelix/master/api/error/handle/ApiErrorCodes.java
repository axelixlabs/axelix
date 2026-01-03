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

/**
 * The error codes that are be returned from the HTTP API.
 *
 * @author Mikhail Polivakha
 */
public enum ApiErrorCodes {
    INTERNAL_SERVER_ERROR_CODE("INTERNAL_SERVER_ERROR"),
    INSTANCE_NOT_FOUND_CODE("INSTANCE_NOT_FOUND"),
    INVALID_CREDENTIALS_CODE("INVALID_CREDENTIALS"),
    INVALID_JWT_EXCEPTION_CODE("INVALID_JWT_EXCEPTION");

    /**
     * actual code that is sent from the master backend.
     */
    private final String errorCode;

    ApiErrorCodes(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
