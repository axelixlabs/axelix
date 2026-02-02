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
 * Represents a request to modify the interval of a scheduled task.
 *
 * @author Sergey Cherkasov
 */
public final class ScheduledTaskIntervalModifyRequest {

    private final String trigger;
    private final Long interval;

    /**
     * Creates a new ScheduledTaskIntervalModifyRequest.
     *
     * @param trigger  the identifier of the scheduled task to modify. Must not be {@code null}.
     * @param interval the new interval to be assigned.
     */
    @JsonCreator
    public ScheduledTaskIntervalModifyRequest(
            @JsonProperty("trigger") String trigger, @JsonProperty("interval") Long interval) {
        this.trigger = trigger;
        this.interval = interval;
    }

    public String getTrigger() {
        return trigger;
    }

    public Long getInterval() {
        return interval;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ScheduledTaskIntervalModifyRequest that = (ScheduledTaskIntervalModifyRequest) o;
        return Objects.equals(trigger, that.trigger) && Objects.equals(interval, that.interval);
    }

    @Override
    public int hashCode() {
        return Objects.hash(trigger, interval);
    }

    @Override
    public String toString() {
        return "ScheduledTaskIntervalModifyRequest{" + "trigger='" + trigger + '\'' + ", interval=" + interval + '}';
    }
}
