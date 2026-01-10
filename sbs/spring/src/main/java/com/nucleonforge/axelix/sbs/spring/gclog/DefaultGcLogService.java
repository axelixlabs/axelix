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
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nucleonforge.axelix.common.api.gclog.GcLogAvailableConfigurationResponse;
import com.nucleonforge.axelix.common.api.gclog.GcLogStatusResponse;

/**
 * Default implementation of {@link GcLogService}.
 *
 * @since 30.12.2025
 * @author Nikita Kirillov
 */
public class DefaultGcLogService implements GcLogService {

    private static final Logger log = LoggerFactory.getLogger(DefaultGcLogService.class);
    private static final String DEFAULT_FILE_NAME = "gc.log";

    private final JcmdExecutor jcmdExecutor;
    private final String pid;
    private final List<String> availableLevels;

    public DefaultGcLogService(JcmdExecutor jcmdExecutor) {
        this.jcmdExecutor = jcmdExecutor;
        this.pid = String.valueOf(ProcessHandle.current().pid());
        this.availableLevels = loadAvailableLevels();
    }

    @Override
    public GcLogAvailableConfigurationResponse getAvailableConfiguration() {
        return new GcLogAvailableConfigurationResponse(availableLevels);
    }

    @Override
    public GcLogStatusResponse getStatus() {
        try {
            ProcessResult result = jcmdExecutor.execute("jcmd", pid, "VM.log", "list");

            return parseStatus(result.getOutput());

        } catch (Exception e) {
            log.warn("Failed to get GC log status", e);
        }

        return new GcLogStatusResponse(false, null);
    }

    @Override
    public File getGcLogFile() {
        return new File(DEFAULT_FILE_NAME);
    }

    @Override
    public void enable(String level) throws GcLogException {
        validateLevel(level);

        try {
            ProcessResult result = jcmdExecutor.execute(
                    "jcmd",
                    pid,
                    "VM.log",
                    "what=gc=" + level.toLowerCase(),
                    "output=file=" + DEFAULT_FILE_NAME,
                    "output_options=filecount=1,filesize=1000K",
                    "decorators=time,level,tags");

            if (!result.isSuccess()) {
                throw new GcLogException(result.getOutput());
            }

            log.info("GC logging enabled: level={}, file={}", level, DEFAULT_FILE_NAME);

        } catch (Exception e) {
            throw new GcLogException("Failed to enable GC logging", e);
        }
    }

    @Override
    public void disable() throws GcLogException {
        try {
            ProcessResult result = jcmdExecutor.execute("jcmd", pid, "VM.log", "disable");

            if (result.isSuccess()) {
                throw new GcLogException(result.getOutput());
            }

        } catch (Exception e) {
            throw new GcLogException("Failed to disable GC logging", e);
        }
    }

    private List<String> loadAvailableLevels() {
        try {
            ProcessResult result = jcmdExecutor.execute("jcmd", pid, "VM.log", "list");

            for (String line : result.getOutput().split("\n")) {
                String trim = line.trim();
                if (trim.startsWith("Available log levels:")) {
                    return Arrays.stream(trim.substring("Available log levels:".length())
                                    .trim()
                                    .split(","))
                            .map(String::trim)
                            .map(String::toLowerCase)
                            .toList();
                }
            }

            throw new GcLogException("Available log levels not found");

        } catch (Exception e) {
            throw new GcLogException("Failed to read JVM log levels", e);
        }
    }

    private GcLogStatusResponse parseStatus(String output) {
        for (String line : output.split("\n")) {
            String trim = line.trim();

            if (trim.startsWith("#") && trim.contains("gc=")) {

                int idx = trim.indexOf("gc=");
                int end = trim.indexOf(" ", idx);
                if (end == -1) {
                    end = trim.length();
                }

                String level = trim.substring(idx + 3, end);

                return new GcLogStatusResponse(true, level);
            }
        }

        return new GcLogStatusResponse(false, null);
    }

    private void validateLevel(String level) {
        if (level == null || level.isBlank()) {
            throw new IllegalArgumentException("Level is empty");
        }

        String normalized = level.toLowerCase();
        if (!availableLevels.contains(normalized)) {
            throw new IllegalArgumentException("Invalid level '" + level + "', available: " + availableLevels);
        }
    }
}
