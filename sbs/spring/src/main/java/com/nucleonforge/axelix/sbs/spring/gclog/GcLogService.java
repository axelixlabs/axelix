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
package com.nucleonforge.axelix.sbs.spring.gclog;

import java.io.File;

import com.nucleonforge.axelix.common.api.gclog.GcLogAvailableConfigurationResponse;
import com.nucleonforge.axelix.common.api.gclog.GcLogStatusResponse;

/**
 * Service for managing JVM GC logging at runtime.
 *
 * @since 10.01.2026
 * @author Nikita Kirillov
 */
public interface GcLogService {

    /**
     * Returns available GC logging configuration supported by the JVM.
     *
     * @return available GC log levels
     */
    GcLogAvailableConfigurationResponse getAvailableConfiguration();

    /**
     * Returns the current GC logging status.
     */
    GcLogStatusResponse getStatus();

    /**
     * Returns the GC log file.
     */
    File getGcLogFile();

    /**
     * Enables GC logging with the given log level.
     *
     * @param level GC log level to enable
     * @throws GcLogException if the level is not supported, or an error occurred enabling logging.
     */
    void enable(String level) throws GcLogException;

    /**
     * Disables GC logging.
     *
     * @throws GcLogException if error occurred disabling logging.
     */
    void disable() throws GcLogException;
}
