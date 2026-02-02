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
package com.axelixlabs.axelix.common.api.scheduledtask;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents a request to toggle (enable/disable) a scheduled task.
 *
 * @since 14.10.2025
 * @author Nikita Kirillov
 * @author Mikhail Polivakha
 */
public final class ScheduledTaskToggleRequest {

    private final String trigger;

    /**
     * Creates a new ScheduledTaskToggleRequest.
     *
     * @param trigger the identifier of the scheduled task to toggle. Must not be {@code null}.
     */
    @JsonCreator
    public ScheduledTaskToggleRequest(@JsonProperty("trigger") String trigger) {
        this.trigger = trigger;
    }

    public String getTrigger() {
        return trigger;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ScheduledTaskToggleRequest that = (ScheduledTaskToggleRequest) o;
        return Objects.equals(trigger, that.trigger);
    }

    @Override
    public int hashCode() {
        return Objects.hash(trigger);
    }

    @Override
    public String toString() {
        return "ScheduledTaskToggleRequest{" + "trigger='" + trigger + '\'' + '}';
    }
}
