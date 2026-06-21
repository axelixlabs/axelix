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
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import org.springframework.boot.logging.LogLevel;
import org.springframework.boot.logging.LoggerConfiguration;
import org.springframework.boot.logging.LoggerGroup;
import org.springframework.boot.logging.LoggerGroups;
import org.springframework.boot.logging.LoggingSystem;

import com.axelixlabs.axelix.common.api.loggers.LogLevelChangeRequest;
import com.axelixlabs.axelix.common.api.loggers.LoggersFeed;
import com.axelixlabs.axelix.common.api.loggers.LoggersGroupProfile;
import com.axelixlabs.axelix.common.api.loggers.SingleLoggerProfile;
import com.axelixlabs.axelix.sbs.spring.core.loggers.exceptions.LogLevelNotFoundException;
import com.axelixlabs.axelix.sbs.spring.core.loggers.exceptions.LoggerNotFoundException;
import com.axelixlabs.axelix.sbs.spring.core.loggers.state.DefaultLoggerChange;
import com.axelixlabs.axelix.sbs.spring.core.loggers.state.LoggerChange;

import static com.axelixlabs.axelix.sbs.spring.core.loggers.exceptions.LogLevelNotFoundException.LOG_LEVEL_REQUIRED_MESSAGE;
import static com.axelixlabs.axelix.sbs.spring.core.loggers.exceptions.LoggerNotFoundException.LOGGER_GROUP_NOT_FOUND_MESSAGE;
import static com.axelixlabs.axelix.sbs.spring.core.loggers.exceptions.LoggerNotFoundException.LOGGER_NOT_FOUND_MESSAGE;
import static java.util.Optional.ofNullable;

/**
 * Default implementation of {@link LoggersService}.
 *
 * @author Nikita Kirillov
 * @author Mikhail Polivakha
 */
public class DefaultLoggersService implements LoggersService {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_OFFSET_DATE_TIME;

    private final LoggingSystem loggingSystem;
    private final LoggerGroups loggerGroups;
    private final ConcurrentMap<String, LoggerChange> configuredLevelsCache;

    public DefaultLoggersService(LoggingSystem loggingSystem, LoggerGroups loggerGroups) {
        this.loggingSystem = loggingSystem;
        this.loggerGroups = loggerGroups;
        // assume that end-users will not change that many loggers manually, 5 at most
        this.configuredLevelsCache = new ConcurrentHashMap<>(5);
    }

    @Override
    public LoggersFeed getAllLoggers() {
        List<LoggerConfiguration> loggerConfigurations = loggingSystem.getLoggerConfigurations();

        return new LoggersFeed(getLogLevels(), getLoggers(loggerConfigurations), getLoggerGroups());
    }

    @Override
    public SingleLoggerProfile getSingleLogger(String loggerName) throws LoggerNotFoundException {
        LoggerConfiguration loggerConfiguration = findLoggerByName(loggerName);

        return convertToSingleLoggerProfile(loggerConfiguration);
    }

    @Override
    public LoggersGroupProfile getLoggerGroup(String groupName) throws LoggerNotFoundException {
        LoggerGroup loggerGroup = findGroupByName(groupName);

        return convertToLoggersGroupProfile(groupName, loggerGroup);
    }

    @Override
    public synchronized void changeLogLevelByLoggerName(String loggerName, LogLevelChangeRequest changeRequest)
            throws LoggerNotFoundException, LogLevelNotFoundException {

        LoggerConfiguration loggerConfiguration = findLoggerByName(loggerName);

        LogLevel targetLevel = convertToLogLevel(changeRequest.getConfiguredLevel());
        Long ttlSeconds = changeRequest.getTtlSeconds();

        LoggerChange loggerChange = configuredLevelsCache.remove(loggerName);

        if (loggerChange == null) {
            LogLevel initiallyConfiguredLevel = loggerConfiguration.getConfiguredLevel();

            configuredLevelsCache.put(
                    loggerName,
                    createAnchor(
                            loggerName,
                            ttlSeconds,
                            ofNullable(initiallyConfiguredLevel).map(Enum::name).orElse(null)));
        } else {
            // Preserving initial configured level as the one that was prior to any changes
            // We cannot just take configured level from the logger system, since if we have the change anchor
            // in the cache, it effectively means we have changed the logging level in runtime already, and
            // the logging system will have the configuredLevel equal to the initial configured level, but to
            // the one that Axelix have configured
            String initiallyConfiguredLevel = loggerChange.getInitialConfiguredLevel();

            // Roll back previous change
            loggerChange.rollbackManually();

            configuredLevelsCache.put(loggerName, createAnchor(loggerName, ttlSeconds, initiallyConfiguredLevel));
        }

        loggingSystem.setLogLevel(loggerName, targetLevel);
    }

    @Override
    public synchronized void changeLogLevelByGroupName(String groupName, LogLevelChangeRequest changeRequest)
            throws LoggerNotFoundException, LogLevelNotFoundException {
        LoggerGroup loggerGroup = findGroupByName(groupName);

        if (loggerGroup.hasMembers()) {
            String configuredLevel = changeRequest.getConfiguredLevel();
            LogLevel logLevel = convertToLogLevel(configuredLevel);

            loggerGroup.configureLogLevel(logLevel, loggingSystem::setLogLevel);
        }
    }

    @Override
    public synchronized void resetLogLevelByLoggerName(String loggerName)
            throws LoggerNotFoundException, LogLevelNotFoundException {
        LoggerChange loggerChange = configuredLevelsCache.remove(loggerName);

        if (loggerChange == null) {
            throw new LoggerNotFoundException("Cannot reset logger");
        }

        loggerChange.rollbackManually();
    }

    private List<String> getLogLevels() {
        Set<LogLevel> levels = new TreeSet<>(loggingSystem.getSupportedLogLevels()).descendingSet();
        return levels.stream().map(Enum::toString).collect(Collectors.toList());
    }

    private DefaultLoggerChange createAnchor(
            String loggerName, @Nullable Long ttlSeconds, @Nullable String initiallyConfiguredLevel) {
        return new DefaultLoggerChange(
                () -> {
                    loggingSystem.setLogLevel(
                            loggerName,
                            Optional.ofNullable(initiallyConfiguredLevel)
                                    .map(LogLevel::valueOf)
                                    .orElse(null));
                    // nullify the reference to the anchor, so it becomes eligible for garbage collection
                    // and also the subsequent invocation will encounter null in the cache
                    configuredLevelsCache.remove(loggerName);
                },
                ofNullable(ttlSeconds).map(Duration::ofSeconds).orElse(null),
                ofNullable(initiallyConfiguredLevel).orElse(null));
    }

    private @NonNull LoggerConfiguration findLoggerByName(String loggerName) {
        return ofNullable(loggingSystem.getLoggerConfiguration(loggerName))
                .orElseThrow(() -> new LoggerNotFoundException(String.format(LOGGER_NOT_FOUND_MESSAGE, loggerName)));
    }

    private @NonNull LoggerGroup findGroupByName(String groupName) {
        return ofNullable(loggerGroups.get(groupName))
                .orElseThrow(
                        () -> new LoggerNotFoundException(String.format(LOGGER_GROUP_NOT_FOUND_MESSAGE, groupName)));
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
        LogLevel configuredLevel = loggerConfiguration.getConfiguredLevel();
        LogLevel effectiveLevel = loggerConfiguration.getEffectiveLevel();

        String loggerName = loggerConfiguration.getName();
        LoggerChange loggerChange = configuredLevelsCache.get(loggerName);

        return new SingleLoggerProfile(
                loggerName,
                ofNullable(configuredLevel).map(Enum::name).orElse(null),
                effectiveLevel.name(),
                ofNullable(loggerChange)
                        .map(LoggerChange::getInitialConfiguredLevel)
                        .orElse(null),
                ofNullable(loggerChange)
                        .map(LoggerChange::getInitiatedAt)
                        .map(it -> it.atOffset(ZoneOffset.UTC))
                        .map(FORMATTER::format)
                        .orElse(null),
                ofNullable(loggerChange)
                        .map(LoggerChange::getAutoRollsBackAt)
                        .map(it -> it.atOffset(ZoneOffset.UTC))
                        .map(FORMATTER::format)
                        .orElse(null));
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
}
