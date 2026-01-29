/*
 * Copyright (C) 2025-2026 Axelix Labs
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package com.axelixlabs.axelix.sbs.spring.gclog;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelixlabs.axelix.common.api.gclog.GcLogStatusResponse;

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

    @Nullable
    private volatile String pid;

    @Nullable
    private volatile List<String> availableLevels;

    public DefaultGcLogService(JcmdExecutor jcmdExecutor) {
        this.jcmdExecutor = jcmdExecutor;
    }

    @Override
    public GcLogStatusResponse getStatus() {
        try {
            ProcessResult result = jcmdExecutor.execute("jcmd", getPid(), "VM.log", "list");

            return parseStatus(result.getOutput());

        } catch (Exception e) {
            throw new GcLogException("Failed to get GC log status via jcmd", e);
        }
    }

    @Override
    public File getGcLogFile() throws GcLogException {
        File file = new File(DEFAULT_FILE_NAME);

        if (!file.exists() || !file.isFile()) {
            throw new GcLogException("GC log file not found");
        }

        return file;
    }

    /**
     * <p><b>Important:</b> The GC log will be written with <b>file rotation enabled</b>
     * (<code>filecount=1, filesize=10M</code>). This is intentional to prevent the log
     * file from growing indefinitely.
     *
     * <p>Although the official Oracle documentation states that the log file may be
     * overwritten, Azul documentation indicates that the file can grow indefinitely.
     * During local testing, the file grew up to 50 MB with no clear upper bound. Therefore,
     * we limit the file to 10 MB and force rotation to ensure predictable log sizes.
     *
     * @see <a href="https://docs.oracle.com/en/java/javase/11/tools/java.html">
     * Oracle Unified JVM Logging - File Rotation Options</a>
     * @see <a href="https://docs.azul.com/prime/Command-Line-Options">
     * Azul JVM Logging Notes</a>
     */
    @Override
    public void enable(String level) throws GcLogException {
        validateLevel(level);

        try {
            ProcessResult result = jcmdExecutor.execute(
                    "jcmd",
                    getPid(),
                    "VM.log",
                    "what=gc=" + level.toLowerCase(),
                    "output=file=" + DEFAULT_FILE_NAME,
                    "output_options=filecount=1,filesize=10M",
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
            ProcessResult result = jcmdExecutor.execute("jcmd", getPid(), "VM.log", "disable");

            if (!result.isSuccess()) {
                throw new GcLogException(result.getOutput());
            }

            log.info("GC logging disabled");

        } catch (Exception e) {
            throw new GcLogException("Failed to disable GC logging", e);
        }
    }

    private String getPid() {
        if (pid == null) {
            synchronized (this) {
                if (pid == null) {
                    pid = String.valueOf(ProcessHandle.current().pid());
                }
            }
        }
        return pid;
    }

    private List<String> getAvailableLevels() {
        if (availableLevels == null) {
            synchronized (this) {
                if (availableLevels == null) {
                    availableLevels = loadAvailableLevels();
                }
            }
        }
        return availableLevels;
    }

    /**
     * <b>Important:</b> The {@code "off"} log level is intentionally excluded from the
     * list of available GC log levels.
     *
     * <p>Disabling GC logging must be performed via {@link GcLogService#disable()}
     * instead of switching the log level to {@code "off"}.
     *
     * <p>This approach is used to preserve existing GC log files after logging
     * is disabled. Applying {@code "off"} as a log level may result in log files
     * being cleared, rotated, or recreated depending on the JVM implementation.
     *
     * <p><b>Note:</b> Verified behavior for Corretto and Liberica JDK distributions
     * across multiple garbage collectors (Serial GC, Parallel GC, G1GC, ZGC, Shenandoah GC).
     * Other JVM implementations may exhibit different behavior.
     */
    private List<String> loadAvailableLevels() {
        try {
            ProcessResult result = jcmdExecutor.execute("jcmd", getPid(), "VM.log", "list");

            for (String line : result.getOutput().split("\n")) {
                String trim = line.trim();

                if (trim.startsWith("Available log levels:")) {
                    return Arrays.stream(trim.substring("Available log levels:".length())
                                    .trim()
                                    .split(","))
                            .map(String::trim)
                            .map(String::toLowerCase)
                            .filter(level -> !level.equals("off"))
                            .toList();
                }
            }

            throw new GcLogException("Available GC log levels not found");

        } catch (Exception e) {
            throw new GcLogException("Failed to read JVM GC log levels", e);
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
                return new GcLogStatusResponse(true, level, getAvailableLevels());
            }
        }

        return new GcLogStatusResponse(false, null, getAvailableLevels());
    }

    private void validateLevel(String level) {
        if (level == null || level.isBlank()) {
            throw new GcLogException("GC log level must not be empty");
        }

        String normalized = level.toLowerCase();
        if (!getAvailableLevels().contains(normalized)) {
            throw new GcLogException("Invalid GC log level '" + level + "', available: " + getAvailableLevels());
        }
    }
}
