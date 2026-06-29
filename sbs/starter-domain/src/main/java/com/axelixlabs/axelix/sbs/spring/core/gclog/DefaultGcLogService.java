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
package com.axelixlabs.axelix.sbs.spring.core.gclog;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import org.jspecify.annotations.Nullable;

import com.axelixlabs.axelix.common.api.gclog.GcLogStatus;
import com.axelixlabs.axelix.sbs.spring.core.log.Logger;

/**
 * Default implementation of {@link GcLogService}.
 *
 * @since 30.12.2025
 * @author Nikita Kirillov
 * @author Sergey Cherkasov
 */
public class DefaultGcLogService implements GcLogService {

    private static final String DEFAULT_FILE_NAME = "gc.log";
    private static final String DEFAULT_LOG_LEVEL = "info";
    private static final String OFF_LOG_LEVEL = "off";

    private final JcmdExecutor jcmdExecutor;
    private final Logger logger;

    @Nullable
    private volatile String pid;

    @Nullable
    private volatile List<String> availableLevels;

    public DefaultGcLogService(JcmdExecutor jcmdExecutor, Logger logger) {
        this.jcmdExecutor = jcmdExecutor;
        this.logger = logger;
    }

    @Override
    public GcLogStatus getStatus() {
        try {
            ProcessResult result = jcmdExecutor.execute("jcmd", getPid(), "VM.log", "list");

            return parseStatus(result.getOutput(), getAvailableLevels());

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

    @Override
    public boolean isGcLogFileSpecified() throws GcLogException {
        try {
            ProcessResult result = jcmdExecutor.execute("jcmd", getPid(), "VM.log", "list");

            for (String line : result.getOutput().split("\n")) {
                String trim = line.trim();
                if (!trim.startsWith("#")) {
                    continue;
                }

                // Expected format: "#0: <output> <what> <decorators> ...", where <what> contains log selectors.
                String[] configurationParts = trim.split("\\s+");
                if (configurationParts.length < 3 || !configurationParts[1].startsWith("file=")) {
                    continue;
                }

                String what = configurationParts[2];
                for (String selectorText : what.split(",")) {
                    if (isGcSelector(selectorText.trim())) {
                        return true;
                    }
                }
            }

            return false;

        } catch (Exception e) {
            throw new GcLogException("Failed to get GC log file output status via jcmd", e);
        }
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
                    "what=gc=" + level.toLowerCase(Locale.ROOT),
                    "output=file=" + DEFAULT_FILE_NAME,
                    "output_options=filecount=1,filesize=10M",
                    "decorators=time,level,tags");

            if (!result.isSuccess()) {
                throw new GcLogException(result.getOutput());
            }

            logger.info("GC logging enabled: level={}, file={}", level, DEFAULT_FILE_NAME);

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

            logger.info("GC logging disabled");

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
                            .map(level -> level.toLowerCase(Locale.ROOT))
                            .filter(level -> !level.equals("off"))
                            .collect(Collectors.toList());
                }
            }

            throw new GcLogException("Available GC log levels not found");

        } catch (Exception e) {
            throw new GcLogException("Failed to read JVM GC log levels", e);
        }
    }

    private void validateLevel(String level) {
        if (level == null || level.isBlank()) {
            throw new GcLogException("GC log level must not be empty");
        }

        String normalized = level.toLowerCase(Locale.ROOT);
        if (!getAvailableLevels().contains(normalized)) {
            throw new GcLogException("Invalid GC log level '" + level + "', available: " + getAvailableLevels());
        }
    }

    private GcLogStatus parseStatus(String output, List<String> availableLevels) {
        boolean gcSelectorFound = false;
        String highestLevel = null;

        for (String line : output.split("\n")) {
            String trim = line.trim();
            if (!trim.startsWith("#")) {
                continue;
            }

            // Expected format: "#0: <output> <what> <decorators> ...", where <what> contains log selectors.
            String[] configurationParts = trim.split("\\s+");
            if (configurationParts.length < 3) {
                continue;
            }

            String what = configurationParts[2];
            for (String selectorText : what.split(",")) {
                String selector = selectorText.trim();

                if (!isGcSelector(selector)) {
                    continue;
                }

                gcSelectorFound = true;
                String[] parts = selector.split("=", 2);
                String level = parts.length == 2 ? parts[1] : DEFAULT_LOG_LEVEL;
                if (!OFF_LOG_LEVEL.equals(level) && verbosity(level) > verbosity(highestLevel)) {
                    highestLevel = level;
                }
            }
        }

        return new GcLogStatus(gcSelectorFound, highestLevel, availableLevels);
    }

    private boolean isGcSelector(String selector) {
        String[] parts = selector.split("=", 2);
        String tagSet = parts[0];

        if ("all".equals(tagSet)) {
            String level = parts.length == 2 ? parts[1] : DEFAULT_LOG_LEVEL;
            // JVM default logging uses all=warning, which technically includes GC warning/error messages.
            // For the product status, treat broad all selectors as GC logging only at info/debug/trace.
            return verbosity(level) >= verbosity(DEFAULT_LOG_LEVEL);
        }

        return "gc".equals(tagSet) || "gc*".equals(tagSet) || tagSet.startsWith("gc+");
    }

    // JEP 158 defines levels in increasing order of verbosity: error < warning < info < debug < trace.
    private int verbosity(@Nullable String level) {
        if (level == null) {
            return -1;
        }

        switch (level) {
            case "error":
                return 0;
            case "warning":
                return 1;
            case "info":
                return 2;
            case "debug":
                return 3;
            case "trace":
                return 4;
            default:
                return -1;
        }
    }
}
