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
package com.axelixlabs.axelix.sbs.spring.core.loggers.state;

import java.time.Instant;

import org.jspecify.annotations.Nullable;

/**
 * Represents the "anchor" of the logger that has been changed.
 * <p>
 * This "anchor" is essentially just a bundle of all the information that we would need,
 * in order to return the initial state of the given logger.
 *
 * @author Mikhail Polivakha
 */
public interface LoggerChange {

    /**
     * @return configured level for the given logger that was in effect before not only this, but any changes from Axelix side.
     * Might be null if there is no initially configured level for the given logger.
     */
    @Nullable
    String getInitialConfiguredLevel();

    /**
     * Returns the timestamp when the change took place. Never {@code null}.
     */
    Instant getInitiatedAt();

    /**
     * Returns the timestamp when this change is supposed to be rolled back automatically. May be {@code null}
     * in case the change is not going to be rolled back automatically.
     */
    @Nullable
    Instant getAutoRollsBackAt();

    /**
     * Cancels the auto-rollback capability.
     */
    void cancelAutoRollback();

    /**
     * Manually rolls back this {@link LoggerChange}.
     */
    void rollbackManually();
}
