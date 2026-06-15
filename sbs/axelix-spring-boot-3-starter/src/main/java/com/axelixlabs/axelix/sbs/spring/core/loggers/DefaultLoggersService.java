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
package com.axelixlabs.axelix.sbs.spring.core.loggers;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.boot.logging.LogLevel;
import org.springframework.boot.logging.LoggerConfiguration;
import org.springframework.boot.logging.LoggerGroup;
import org.springframework.boot.logging.LoggerGroups;
import org.springframework.boot.logging.LoggingSystem;

import com.axelixlabs.axelix.common.api.loggers.LogLevelChangeRequest;
import com.axelixlabs.axelix.common.api.loggers.LoggersFeed;
import com.axelixlabs.axelix.common.api.loggers.LoggersGroupProfile;
import com.axelixlabs.axelix.common.api.loggers.SingleLoggerProfile;

import static com.axelixlabs.axelix.sbs.spring.core.loggers.LogLevelNotFoundException.LOG_LEVEL_REQUIRED_MESSAGE;
import static com.axelixlabs.axelix.sbs.spring.core.loggers.LoggerNotFoundException.LOGGER_GROUP_NOT_FOUND_MESSAGE;
import static com.axelixlabs.axelix.sbs.spring.core.loggers.LoggerNotFoundException.LOGGER_NOT_FOUND_MESSAGE;

/**
 * Default implementation of {@link LoggersService}.
 *
 * @author Nikita Kirillov
 */
public class DefaultLoggersService implements LoggersService, DisposableBean {

    private final LoggingSystem loggingSystem;
    private final LoggerGroups loggerGroups;

    private final ScheduledExecutorService scheduler;
    private final ConcurrentMap<String, OriginalLoggerState> cacheOriginalLogLevels;
    private final ConcurrentMap<String, ScheduledLogLevelOverride> scheduledOverrideLoggers;
    private final ConcurrentMap<String, ScheduledFuture<?>> scheduledTasks = new ConcurrentHashMap<>();

    public DefaultLoggersService(LoggingSystem loggingSystem, LoggerGroups loggerGroups) {
        this.loggingSystem = loggingSystem;
        this.loggerGroups = loggerGroups;

        List<LoggerConfiguration> loggerConfigurations = this.loggingSystem.getLoggerConfigurations();
        // Not critical, but pre-sizes the map to avoid overhead from bucket resizing when loggers are added dynamically
        this.cacheOriginalLogLevels = new ConcurrentHashMap<>((int) (loggerConfigurations.size() * 1.3f));
        updateCacheOriginalLogLevels(loggerConfigurations);

        this.scheduledOverrideLoggers = new ConcurrentHashMap<>();
        this.scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread thread = new Thread(r, "axelix-loggers-expiry");
            thread.setDaemon(true);
            return thread;
        });
    }

    @Override
    public LoggersFeed getAllLoggers() {
        List<LoggerConfiguration> loggerConfigurations = loggingSystem.getLoggerConfigurations();
        if (loggerConfigurations == null || loggerConfigurations.isEmpty()) {
            return new LoggersFeed(Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
        }

        // Picks up lazily-initialized loggers that weren't present at startup
        updateCacheOriginalLogLevels(loggerConfigurations);

        return new LoggersFeed(getLogLevels(), getLoggers(loggerConfigurations), getLoggerGroups());
    }

    @Override
    public SingleLoggerProfile getSingleLogger(String loggerName) throws LoggerNotFoundException {
        LoggerConfiguration loggerConfiguration = loggingSystem.getLoggerConfiguration(loggerName);
        if (loggerConfiguration == null) {
            throw new LoggerNotFoundException(LOGGER_NOT_FOUND_MESSAGE.formatted(loggerName));
        }
        updateCacheOriginalLogLevels(List.of(loggerConfiguration));

        return convertToSingleLoggerProfile(loggerConfiguration);
    }

    @Override
    public LoggersGroupProfile getLoggerGroup(String groupName) throws LoggerNotFoundException {
        LoggerGroup loggerGroup = loggerGroups.get(groupName);
        if (loggerGroup == null) {
            throw new LoggerNotFoundException(LOGGER_GROUP_NOT_FOUND_MESSAGE.formatted(groupName));
        }

        return convertToLoggersGroupProfile(groupName, loggerGroup);
    }

    @Override
    public void changeLogLevelByLoggerName(String loggerName, LogLevelChangeRequest changeRequest)
            throws LoggerNotFoundException, LogLevelNotFoundException {

        LoggerConfiguration loggerConfiguration = loggingSystem.getLoggerConfiguration(loggerName);

        if (loggerConfiguration == null) {
            throw new LoggerNotFoundException(LOGGER_NOT_FOUND_MESSAGE.formatted(loggerName));
        }

        LogLevel targetLevel = convertToLogLevel(changeRequest.getConfiguredLevel());

        updateCacheOriginalLogLevels(List.of(loggerConfiguration));

        loggingSystem.setLogLevel(loggerName, targetLevel);

        Long ttlMinutes = changeRequest.getTtlMinutes();
        if (ttlMinutes != null) {
            scheduleLogLevelReset(loggerName, ttlMinutes);
        } else {
            cancelScheduledTask(loggerName);
        }
    }

    @Override
    public void changeLogLevelByGroupName(String groupName, LogLevelChangeRequest changeRequest)
            throws LoggerNotFoundException, LogLevelNotFoundException {
        LoggerGroup loggerGroup = loggerGroups.get(groupName);

        if (loggerGroup != null && loggerGroup.hasMembers()) {
            String configuredLevel = changeRequest.getConfiguredLevel();
            LogLevel logLevel = convertToLogLevel(configuredLevel);

            loggerGroup.configureLogLevel(logLevel, loggingSystem::setLogLevel);
            return;
        }

        throw new LoggerNotFoundException(LOGGER_GROUP_NOT_FOUND_MESSAGE.formatted(groupName));
    }

    @Override
    public void resetLogLevelByLoggerName(String loggerName) throws LoggerNotFoundException, LogLevelNotFoundException {
        LoggerConfiguration loggerConfiguration = loggingSystem.getLoggerConfiguration(loggerName);

        if (loggerConfiguration == null) {
            throw new LoggerNotFoundException(LOGGER_NOT_FOUND_MESSAGE.formatted(loggerName));
        }
        cancelScheduledTask(loggerName);
        updateCacheOriginalLogLevels(List.of(loggerConfiguration));

        OriginalLoggerState originalState = cacheOriginalLogLevels.get(loggerName);

        LogLevel targetLevel = (originalState != null && originalState.getConfiguredLevel() != null)
                ? convertToLogLevel(originalState.getConfiguredLevel())
                : null;

        // The configured level can be null, which correctly restores the inheritance from its parent logger
        loggingSystem.setLogLevel(loggerName, targetLevel);
    }

    private void updateCacheOriginalLogLevels(List<LoggerConfiguration> loggerConfigurations) {
        if (loggerConfigurations == null || loggerConfigurations.isEmpty()) {
            return;
        }

        for (LoggerConfiguration loggerConfig : loggerConfigurations) {
            LogLevel configuredLevel = loggerConfig.getConfiguredLevel();
            cacheOriginalLogLevels.putIfAbsent(
                    loggerConfig.getName(),
                    new OriginalLoggerState(
                            loggerConfig.getEffectiveLevel().toString(),
                            configuredLevel != null ? configuredLevel.toString() : null));
        }
    }

    private List<String> getLogLevels() {
        Set<LogLevel> levels = new TreeSet<>(loggingSystem.getSupportedLogLevels()).descendingSet();
        return levels.stream().map(Enum::toString).toList();
    }

    private List<SingleLoggerProfile> getLoggers(List<LoggerConfiguration> loggerConfigurations) {
        List<SingleLoggerProfile> loggers = new ArrayList<>(loggerConfigurations.size());

        for (LoggerConfiguration loggerConfig : loggerConfigurations) {
            loggers.add(convertToSingleLoggerProfile(loggerConfig));
        }

        return loggers;
    }

    private List<LoggersGroupProfile> getLoggerGroups() {
        List<LoggersGroupProfile> groups = new ArrayList<>();

        loggerGroups.forEach((group) -> {
            LogLevel logLevel = group.getConfiguredLevel();
            String configuredLevel = logLevel != null ? logLevel.toString() : null;

            groups.add(new LoggersGroupProfile(group.getName(), configuredLevel, group.getMembers()));
        });
        return groups;
    }

    private SingleLoggerProfile convertToSingleLoggerProfile(LoggerConfiguration loggerConfiguration) {

        LogLevel configuredLogLevel = loggerConfiguration.getConfiguredLevel();
        String loggerName = loggerConfiguration.getName();
        String configuredLevel = configuredLogLevel != null ? configuredLogLevel.toString() : null;
        String effectiveLevel = loggerConfiguration.getEffectiveLevel().toString();

        boolean isOriginalLevel = false;
        OriginalLoggerState originalState = cacheOriginalLogLevels.get(loggerName);

        if (originalState != null) {
            isOriginalLevel = Objects.equals(configuredLevel, originalState.getConfiguredLevel())
                    && Objects.equals(effectiveLevel, originalState.getEffectiveLevel());
        }

        String temporaryLevelAppliedAt = null;
        String temporaryLevelExpiresAt = null;

        ScheduledLogLevelOverride scheduledOverride = scheduledOverrideLoggers.get(loggerName);
        if (scheduledOverride != null) {
            temporaryLevelAppliedAt = scheduledOverride.getAppliedAt();
            temporaryLevelExpiresAt = scheduledOverride.getExpiresAt();
        }

        return new SingleLoggerProfile(
                loggerName,
                configuredLevel,
                effectiveLevel,
                isOriginalLevel,
                temporaryLevelAppliedAt,
                temporaryLevelExpiresAt);
    }

    private LoggersGroupProfile convertToLoggersGroupProfile(String groupName, LoggerGroup loggerGroup) {
        LogLevel logLevel = loggerGroup.getConfiguredLevel();

        String configuredLevel = logLevel != null ? logLevel.name() : null;

        return new LoggersGroupProfile(groupName, configuredLevel, loggerGroup.getMembers());
    }

    private LogLevel convertToLogLevel(String level) throws LogLevelNotFoundException {
        if (level == null || level.isBlank()) {
            throw new LogLevelNotFoundException(LOG_LEVEL_REQUIRED_MESSAGE);
        }
        try {
            return LogLevel.valueOf(level.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new LogLevelNotFoundException(level, getLogLevels(), e);
        }
    }

    private void scheduleLogLevelReset(String loggerName, Long ttlMinutes) {
        ScheduledFuture<?> existingTask = scheduledTasks.get(loggerName);
        if (existingTask != null) {
            existingTask.cancel(false);
            scheduledTasks.remove(loggerName);
            scheduledOverrideLoggers.remove(loggerName);
        }

        Instant now = Instant.now();
        // Guards against stale reset tasks overwriting a newer override, since cancel(false)
        // cannot interrupt an already-running task.
        long generation = now.toEpochMilli();

        ScheduledLogLevelOverride override = new ScheduledLogLevelOverride(
                now.toString(), now.plus(Duration.ofMinutes(ttlMinutes)).toString(), generation);

        scheduledOverrideLoggers.put(loggerName, override);

        ScheduledFuture<?> future = scheduler.schedule(
                () -> {
                    ScheduledLogLevelOverride current = scheduledOverrideLoggers.get(loggerName);
                    if (current != null && current.getGeneration() == generation) {
                        scheduledOverrideLoggers.remove(loggerName);
                        scheduledTasks.remove(loggerName);
                        resetLogLevelByLoggerName(loggerName);
                    }
                },
                ttlMinutes,
                TimeUnit.MINUTES);

        scheduledTasks.put(loggerName, future);
    }

    private void cancelScheduledTask(String loggerName) {
        scheduledOverrideLoggers.remove(loggerName);
        ScheduledFuture<?> existingTask = scheduledTasks.remove(loggerName);
        if (existingTask != null) {
            existingTask.cancel(false);
        }
    }

    @Override
    public void destroy() {
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(1, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
