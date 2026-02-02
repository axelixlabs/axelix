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
 * Represents a request to forcibly execute a scheduled task.
 *
 * @author Sergey Cherkasov
 */
public final class ScheduledTaskExecuteRequest {

    private final String trigger;

    /**
     * Creates a new ScheduledTaskExecuteRequest.
     *
     * @param trigger the identifier of the scheduled task to run now. Must not be {@code null}.
     */
    @JsonCreator
    public ScheduledTaskExecuteRequest(@JsonProperty("trigger") String trigger) {
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
        ScheduledTaskExecuteRequest that = (ScheduledTaskExecuteRequest) o;
        return Objects.equals(trigger, that.trigger);
    }

    @Override
    public int hashCode() {
        return Objects.hash(trigger);
    }

    @Override
    public String toString() {
        return "ScheduledTaskExecuteRequest{" + "trigger='" + trigger + '\'' + '}';
    }
}
