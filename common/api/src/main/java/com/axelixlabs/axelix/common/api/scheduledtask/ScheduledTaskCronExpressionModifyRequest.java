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
 * Represents a request to modify the cron expression of a scheduled task.
 *
 * @author Sergey Cherkasov
 */
public final class ScheduledTaskCronExpressionModifyRequest {

    private final String trigger;
    private final String cronExpression;

    /**
     * Creates a new ScheduledTaskCronExpressionModifyRequest.
     *
     * @param trigger        the identifier of the scheduled task to modify. Must not be {@code null}.
     * @param cronExpression the new cron expression to be assigned.
     */
    @JsonCreator
    public ScheduledTaskCronExpressionModifyRequest(
            @JsonProperty("trigger") String trigger, @JsonProperty("cronExpression") String cronExpression) {
        this.trigger = trigger;
        this.cronExpression = cronExpression;
    }

    public String getTrigger() {
        return trigger;
    }

    public String getCronExpression() {
        return cronExpression;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ScheduledTaskCronExpressionModifyRequest that = (ScheduledTaskCronExpressionModifyRequest) o;
        return Objects.equals(trigger, that.trigger) && Objects.equals(cronExpression, that.cronExpression);
    }

    @Override
    public int hashCode() {
        return Objects.hash(trigger, cronExpression);
    }

    @Override
    public String toString() {
        return "ScheduledTaskCronExpressionModifyRequest{"
                + "trigger='"
                + trigger
                + '\''
                + ", cronExpression='"
                + cronExpression
                + '\''
                + '}';
    }
}
