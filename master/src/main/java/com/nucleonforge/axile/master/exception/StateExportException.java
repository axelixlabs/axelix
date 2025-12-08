/*
 * Copyright 2025-present the original author or authors.
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
package com.nucleonforge.axile.master.exception;

import com.nucleonforge.axile.master.service.export.ZipArchiveInstanceStateExporter;

/**
 * Typically thrown by the {@link ZipArchiveInstanceStateExporter} when state export
 * operations fail due to IO errors, data collection issues, or archive creation problems.
 *
 * @since 27.10.2025
 * @author Nikita Kirillov
 */
public class StateExportException extends RuntimeException {

    public StateExportException() {}

    public StateExportException(String instanceId, Throwable cause) {
        super("State export failed for instance: " + instanceId, cause);
    }

    public StateExportException(String instanceId, String message) {
        super(String.format("State export failed for instance: %s. %s", instanceId, message));
    }
}
