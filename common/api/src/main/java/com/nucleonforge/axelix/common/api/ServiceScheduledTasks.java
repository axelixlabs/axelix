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
package com.nucleonforge.axelix.common.api;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.jspecify.annotations.Nullable;

import com.nucleonforge.axelix.common.domain.spring.actuator.ActuatorEndpoints;

/**
 * The response of the {@link ActuatorEndpoints#GET_SCHEDULED_TASKS} actuator endpoint provides information about the application’s scheduled tasks.
 *
 * @apiNote <a href="https://docs.spring.io/spring-boot/api/rest/actuator/scheduledtasks.html">Scheduled Tasks Endpoint</a>
 *
 * @param cron          The list of cron scheduled tasks with precise execution configuration, if any.
 * @param fixedDelay    The list of scheduled interval between tasks executions, counted from the end of the previous task execution, if any.
 * @param fixedRate     The list of scheduled interval between task executions, measured from the start of the previous task execution, if any.
 * @param custom        The list of tasks with a configured user triggers, if any.
 *
 * @author Sergey Cherkasov
 */
public record ServiceScheduledTasks(
        @JsonProperty("cron") List<CronTask> cron,
        @JsonProperty("fixedDelay") List<FixedDelayTask> fixedDelay,
        @JsonProperty("fixedRate") List<FixedRateTask> fixedRate,
        @JsonProperty("custom") List<CustomTask> custom) {

    /**
     * DTO representing a scheduled cron task with precise execution configuration.
     *
     * @param runnable         The target that will be executed.
     * @param expression       The cron expression (e.g., "0 1 1 5 7 3" or "0 0/15 9-17 ? * MON,WED,FRI" (seconds minutes hours day_of_month month day_of_week))
     * @param nextExecution    The time of the next scheduled execution of this task, if known.
     * @param lastExecution    The last execution of this task, if any.
     * @param enabled   The indicator showing whether the cron task is enabled {@code true} or disabled {@code false}.
     *
     * @author Sergey Cherkasov
     */
    public record CronTask(
            @JsonProperty("runnable") Runnable runnable,
            @JsonProperty("expression") String expression,
            @JsonProperty("nextExecution") @Nullable NextExecution nextExecution,
            @JsonProperty("lastExecution") @Nullable LastExecution lastExecution,
            @JsonProperty("enabled") boolean enabled) {}

    /**
     * DTO representing the interval between task executions, counted from the end of the previous task execution.
     *
     * @param runnable         The target that will be executed.
     * @param interval         The interval, in milliseconds, between the start of each execution.
     * @param initialDelay     The delay, in milliseconds, before first execution.
     * @param nextExecution    The time of the next scheduled execution of this task, if known.
     * @param lastExecution    The last execution of this task, if any.
     * @param enabled   The indicator showing whether the cron task is enabled {@code true} or disabled {@code false}.
     *
     * @author Sergey Cherkasov
     */
    public record FixedDelayTask(
            @JsonProperty("runnable") Runnable runnable,
            @JsonProperty("interval") Number interval,
            @JsonProperty("initialDelay") Number initialDelay,
            @JsonProperty("nextExecution") @Nullable NextExecution nextExecution,
            @JsonProperty("lastExecution") @Nullable LastExecution lastExecution,
            @JsonProperty("enabled") boolean enabled) {}

    /**
     * DTO representing the interval between task executions, measured from the start of the previous task execution.
     *
     * @param runnable         The target that will be executed.
     * @param interval         The interval, in milliseconds, between the start of each execution.
     * @param initialDelay     The delay, in milliseconds, before first execution.
     * @param nextExecution    The time of the next scheduled execution of this task, if known.
     * @param lastExecution    The last execution of this task, if any.
     * @param enabled   The indicator showing whether the cron task is enabled {@code true} or disabled {@code false}.
     *
     * @author Sergey Cherkasov
     */
    public record FixedRateTask(
            @JsonProperty("runnable") Runnable runnable,
            @JsonProperty("interval") Number interval,
            @JsonProperty("initialDelay") Number initialDelay,
            @JsonProperty("nextExecution") @Nullable NextExecution nextExecution,
            @JsonProperty("lastExecution") @Nullable LastExecution lastExecution,
            @JsonProperty("enabled") boolean enabled) {}

    /**
     * DTO representing a task with a configured user trigger.
     *
     * @param runnable        The target that will be executed.
     * @param trigger         The trigger used to execute this task.
     * @param nextExecution    The time of the next scheduled execution of this task, if known.
     * @param lastExecution   The last execution of this task, if any.
     * @param enabled   The indicator showing whether the cron task is enabled {@code true} or disabled {@code false}.
     *
     * @author Sergey Cherkasov
     */
    public record CustomTask(
            @JsonProperty("runnable") Runnable runnable,
            @JsonProperty("trigger") String trigger,
            @JsonProperty("nextExecution") @Nullable NextExecution nextExecution,
            @JsonProperty("lastExecution") @Nullable LastExecution lastExecution,
            @JsonProperty("enabled") boolean enabled) {}

    /**
     * DTO representing the last execution of a task.
     *
     * @param status      The status of the last execution of a task (STARTED, SUCCESS, ERROR).
     * @param time        The time of the last execution of a task.
     * @param exception   The exception that may occur, if any.
     *
     * @author Sergey Cherkasov
     */
    public record LastExecution(
            @JsonProperty("status") String status,
            @JsonProperty("time") String time,
            @JsonProperty("exception") @Nullable Exception exception) {

        /**
         * DTO representing a possible exception.
         *
         * @param type      The type of exception thrown by the task, if any.
         * @param message   The message of the exception thrown by the task, if any.
         *
         * @author Sergey Cherkasov
         */
        public record Exception(@JsonProperty("type") String type, @JsonProperty("message") String message) {}
    }

    /**
     * DTO that contains the next planned execution time of task.
     *
     * @author Sergey Cherkasov
     */
    public record NextExecution(@JsonProperty("time") String time) {}

    /**
     * DTO that contains the target that will be executed.
     *
     * @param target The target for execution.
     *
     * @author Sergey Cherkasov
     */
    public record Runnable(@JsonProperty("target") String target) {}
}
