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
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import com.axelixlabs.axelix.common.utils.Lazy;

/**
 * Default implementation of the {@link LoggerChange}.
 *
 * @author Mikhail Polivakha
 */
class DefaultLoggerChange implements LoggerChange {

    private static final Lazy<ScheduledExecutorService> CLEANUP_SCHEDULED;

    private final Instant initiatedAt;

    @Nullable
    private final String initialConfiguredLevel;

    @Nullable
    private final Duration changeDuration;

    private final Runnable rollbackAction;

    @Nullable
    private final ScheduledFuture<?> automaticRollbackTask;

    static {
        CLEANUP_SCHEDULED = Lazy.of(DefaultLoggerChange::createCleanerScheduledExecutorService);
    }

    private static @NonNull ScheduledExecutorService createCleanerScheduledExecutorService() {
        return Executors.newSingleThreadScheduledExecutor(r -> {
            Thread thread = new Thread(r, "axelix-loggers-expiry");
            thread.setDaemon(true);
            return thread;
        });
    }

    @SuppressWarnings("NullAway") // for Lazy.get
    public DefaultLoggerChange(
            Runnable rollbackAction, @Nullable Duration changeDuration, @Nullable String initialConfiguredLevel) {
        this.initialConfiguredLevel = initialConfiguredLevel;
        this.rollbackAction = rollbackAction;
        this.initiatedAt = Instant.now();

        if (changeDuration != null) {
            this.changeDuration = changeDuration;
            this.automaticRollbackTask = CLEANUP_SCHEDULED
                    .get()
                    .schedule(this.rollbackAction, changeDuration.getSeconds(), TimeUnit.SECONDS);
        } else {
            this.changeDuration = null;
            this.automaticRollbackTask = null;
        }
    }

    @Override
    @Nullable
    public String getInitialConfiguredLevel() {
        return initialConfiguredLevel;
    }

    public Instant getInitiatedAt() {
        return initiatedAt;
    }

    @Override
    public @Nullable Instant getAutoRollsBackAt() {
        if (changeDuration != null) {
            return initiatedAt.plusSeconds(changeDuration.toSeconds());
        } else {
            return null;
        }
    }

    @Override
    public void rollbackManually() {
        if (automaticRollbackTask != null) {
            automaticRollbackTask.cancel(false);
        }
        this.rollbackAction.run();
    }
}
