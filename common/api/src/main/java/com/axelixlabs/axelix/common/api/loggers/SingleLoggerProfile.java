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
package com.axelixlabs.axelix.common.api.loggers;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.jspecify.annotations.Nullable;

/**
 * DTO that encapsulates the logging level information of the single logger.
 *
 * @author Sergey Cherkasov
 * @author Nikita Kirillov
 */
public final class SingleLoggerProfile {

    /**
     * The name of the logger.
     */
    private final String name;

    /**
     * The explicitly configured logging level for this specific logger.
     * Returns {@code null} if no explicit level is set.
     */
    @Nullable
    private final String configuredLevel;

    /**
     * The actual, operational logging level currently in use.
     * If {@link #configuredLevel} is missing, this is inherited from the parent logger hierarchy.
     */
    private final String effectiveLevel;

    /**
     * The logging level onto which the {@link #configuredLevel} is going to change if this {@link #configuredLevel} is
     * temporary (i.e. if {@link #temporaryLevelInitiatedAt} is noy {@code null}).
     */
    @Nullable
    private final String fallbackLevel;

    /**
     * The timestamp indicating when the temporary logging level was activated.
     * Returns {@code null} if the current level is permanent.
     */
    @Nullable
    private final String temporaryLevelInitiatedAt;

    /**
     * The timestamp indicating when the temporary logging level will expire and revert.
     * Returns {@code null} if there is no expiration time set.
     */
    @Nullable
    private final String temporaryLevelRollsBackAt;

    public SingleLoggerProfile(
            @JsonProperty("name") String name,
            @JsonProperty("configuredLevel") @Nullable String configuredLevel,
            @JsonProperty("effectiveLevel") String effectiveLevel,
            @JsonProperty("fallbackLevel") @Nullable String fallbackLevel,
            @JsonProperty("temporaryLevelInitiatedAt") @Nullable String temporaryLevelInitiatedAt,
            @JsonProperty("temporaryLevelRollsBackAt") @Nullable String temporaryLevelRollsBackAt) {
        this.name = name;
        this.configuredLevel = configuredLevel;
        this.effectiveLevel = effectiveLevel;
        this.fallbackLevel = fallbackLevel;
        this.temporaryLevelInitiatedAt = temporaryLevelInitiatedAt;
        this.temporaryLevelRollsBackAt = temporaryLevelRollsBackAt;
    }

    public String getName() {
        return name;
    }

    public @Nullable String getConfiguredLevel() {
        return configuredLevel;
    }

    public String getEffectiveLevel() {
        return effectiveLevel;
    }

    @Nullable
    public String getFallbackLevel() {
        return fallbackLevel;
    }

    public @Nullable String getTemporaryLevelInitiatedAt() {
        return temporaryLevelInitiatedAt;
    }

    public @Nullable String getTemporaryLevelRollsBackAt() {
        return temporaryLevelRollsBackAt;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof SingleLoggerProfile)) {
            return false;
        }
        SingleLoggerProfile that = (SingleLoggerProfile) o;
        return Objects.equals(name, that.name)
                && Objects.equals(configuredLevel, that.configuredLevel)
                && Objects.equals(effectiveLevel, that.effectiveLevel)
                && Objects.equals(temporaryLevelInitiatedAt, that.temporaryLevelInitiatedAt)
                && Objects.equals(temporaryLevelRollsBackAt, that.temporaryLevelRollsBackAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                name, configuredLevel, effectiveLevel, temporaryLevelInitiatedAt, temporaryLevelRollsBackAt);
    }

    @Override
    public String toString() {
        return "SingleLoggerProfile{" + "name='"
                + name + '\'' + ", configuredLevel='"
                + configuredLevel + '\'' + ", effectiveLevel='"
                + effectiveLevel + '\'' + ", fallbackLevel='"
                + fallbackLevel + '\'' + ", temporaryLevelInitiatedAt='"
                + temporaryLevelInitiatedAt + '\'' + ", temporaryLevelRollsBackAt='"
                + temporaryLevelRollsBackAt + '\'' + '}';
    }
}
