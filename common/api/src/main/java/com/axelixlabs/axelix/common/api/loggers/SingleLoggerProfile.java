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
     * The initial logging level before any temporary modifications were applied.
     */
    private final boolean isOriginalLevel;

    /**
     * The timestamp indicating when the temporary logging level was activated.
     * Returns {@code null} if the current level is permanent.
     */
    @Nullable
    private final String temporaryLevelAppliedAt;

    /**
     * The timestamp indicating when the temporary logging level will expire and revert.
     * Returns {@code null} if there is no expiration time set.
     */
    @Nullable
    private final String temporaryLevelExpiresAt;

    public SingleLoggerProfile(
            @JsonProperty("name") String name,
            @JsonProperty("configuredLevel") @Nullable String configuredLevel,
            @JsonProperty("effectiveLevel") String effectiveLevel,
            @JsonProperty("isOriginalLevel") boolean isOriginalLevel,
            @JsonProperty("temporaryLevelAppliedAt") @Nullable String temporaryLevelAppliedAt,
            @JsonProperty("temporaryLevelExpiresAt") @Nullable String temporaryLevelExpiresAt) {
        this.name = name;
        this.configuredLevel = configuredLevel;
        this.effectiveLevel = effectiveLevel;
        this.isOriginalLevel = isOriginalLevel;
        this.temporaryLevelAppliedAt = temporaryLevelAppliedAt;
        this.temporaryLevelExpiresAt = temporaryLevelExpiresAt;
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

    public boolean isOriginalLevel() {
        return isOriginalLevel;
    }

    public @Nullable String getTemporaryLevelAppliedAt() {
        return temporaryLevelAppliedAt;
    }

    public @Nullable String getTemporaryLevelExpiresAt() {
        return temporaryLevelExpiresAt;
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
                && Objects.equals(isOriginalLevel, that.isOriginalLevel)
                && Objects.equals(temporaryLevelAppliedAt, that.temporaryLevelAppliedAt)
                && Objects.equals(temporaryLevelExpiresAt, that.temporaryLevelExpiresAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                name,
                configuredLevel,
                effectiveLevel,
                isOriginalLevel,
                temporaryLevelAppliedAt,
                temporaryLevelExpiresAt);
    }

    @Override
    public String toString() {
        return "SingleLoggerProfile{" + "name='"
                + name + '\'' + ", configuredLevel='"
                + configuredLevel + '\'' + ", effectiveLevel='"
                + effectiveLevel + '\'' + ", isOriginalLevel="
                + isOriginalLevel + ", temporaryLevelAppliedAt='"
                + temporaryLevelAppliedAt + '\'' + ", temporaryLevelExpiresAt='"
                + temporaryLevelExpiresAt + '\'' + '}';
    }
}
