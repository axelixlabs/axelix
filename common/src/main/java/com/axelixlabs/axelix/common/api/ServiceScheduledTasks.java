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
package com.axelixlabs.axelix.common.api;

import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.jspecify.annotations.Nullable;

/**
 * The response of the scheduled tasks feed endpoint that provides information about the application's scheduled tasks.
 *
 * @apiNote <a href="https://docs.spring.io/spring-boot/api/rest/actuator/scheduledtasks.html">Scheduled Tasks Endpoint</a>
 *
 * @author Sergey Cherkasov
 */
public final class ServiceScheduledTasks {

    private final List<CronTask> cron;
    private final List<FixedDelayTask> fixedDelay;
    private final List<FixedRateTask> fixedRate;
    private final List<CustomTask> custom;

    /**
     * Creates a new ServiceScheduledTasks.
     *
     * @param cron       The list of cron scheduled tasks with precise execution configuration, if any.
     * @param fixedDelay The list of scheduled interval between tasks executions, counted from the end of the previous task execution, if any.
     * @param fixedRate  The list of scheduled interval between task executions, measured from the start of the previous task execution, if any.
     * @param custom     The list of tasks with a configured user triggers, if any.
     */
    @JsonCreator
    public ServiceScheduledTasks(
            @JsonProperty("cron") List<CronTask> cron,
            @JsonProperty("fixedDelay") List<FixedDelayTask> fixedDelay,
            @JsonProperty("fixedRate") List<FixedRateTask> fixedRate,
            @JsonProperty("custom") List<CustomTask> custom) {
        this.cron = cron;
        this.fixedDelay = fixedDelay;
        this.fixedRate = fixedRate;
        this.custom = custom;
    }

    public List<CronTask> getCron() {
        return cron;
    }

    public List<FixedDelayTask> getFixedDelay() {
        return fixedDelay;
    }

    public List<FixedRateTask> getFixedRate() {
        return fixedRate;
    }

    public List<CustomTask> getCustom() {
        return custom;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ServiceScheduledTasks that = (ServiceScheduledTasks) o;
        return Objects.equals(cron, that.cron)
                && Objects.equals(fixedDelay, that.fixedDelay)
                && Objects.equals(fixedRate, that.fixedRate)
                && Objects.equals(custom, that.custom);
    }

    @Override
    public int hashCode() {
        return Objects.hash(cron, fixedDelay, fixedRate, custom);
    }

    @Override
    public String toString() {
        return "ServiceScheduledTasks{"
                + "cron="
                + cron
                + ", fixedDelay="
                + fixedDelay
                + ", fixedRate="
                + fixedRate
                + ", custom="
                + custom
                + '}';
    }

    /**
     * DTO representing a scheduled cron task with precise execution configuration.
     *
     * @author Sergey Cherkasov
     */
    public static final class CronTask {

        private final Runnable runnable;
        private final String expression;

        @Nullable
        private final NextExecution nextExecution;

        @Nullable
        private final LastExecution lastExecution;

        private final boolean enabled;

        /**
         * Creates a new CronTask.
         *
         * @param runnable      The target that will be executed.
         * @param expression    The cron expression (e.g., "0 1 1 5 7 3" or "0 0/15 9-17 ? * MON,WED,FRI" (seconds minutes hours day_of_month month day_of_week))
         * @param nextExecution The time of the next scheduled execution of this task, if known.
         * @param lastExecution The last execution of this task, if any.
         * @param enabled       The indicator showing whether the cron task is enabled {@code true} or disabled {@code false}.
         */
        @JsonCreator
        public CronTask(
                @JsonProperty("runnable") Runnable runnable,
                @JsonProperty("expression") String expression,
                @JsonProperty("nextExecution") @Nullable NextExecution nextExecution,
                @JsonProperty("lastExecution") @Nullable LastExecution lastExecution,
                @JsonProperty("enabled") boolean enabled) {
            this.runnable = runnable;
            this.expression = expression;
            this.nextExecution = nextExecution;
            this.lastExecution = lastExecution;
            this.enabled = enabled;
        }

        public Runnable getRunnable() {
            return runnable;
        }

        public String getExpression() {
            return expression;
        }

        @Nullable
        public NextExecution getNextExecution() {
            return nextExecution;
        }

        @Nullable
        public LastExecution getLastExecution() {
            return lastExecution;
        }

        public boolean isEnabled() {
            return enabled;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            CronTask cronTask = (CronTask) o;
            return enabled == cronTask.enabled
                    && Objects.equals(runnable, cronTask.runnable)
                    && Objects.equals(expression, cronTask.expression)
                    && Objects.equals(nextExecution, cronTask.nextExecution)
                    && Objects.equals(lastExecution, cronTask.lastExecution);
        }

        @Override
        public int hashCode() {
            return Objects.hash(runnable, expression, nextExecution, lastExecution, enabled);
        }

        @Override
        public String toString() {
            return "CronTask{"
                    + "runnable="
                    + runnable
                    + ", expression='"
                    + expression
                    + '\''
                    + ", nextExecution="
                    + nextExecution
                    + ", lastExecution="
                    + lastExecution
                    + ", enabled="
                    + enabled
                    + '}';
        }
    }

    /**
     * DTO representing the interval between task executions, counted from the end of the previous task execution.
     *
     * @author Sergey Cherkasov
     */
    public static final class FixedDelayTask {

        private final Runnable runnable;
        private final Number interval;
        private final Number initialDelay;

        @Nullable
        private final NextExecution nextExecution;

        @Nullable
        private final LastExecution lastExecution;

        private final boolean enabled;

        /**
         * Creates a new FixedDelayTask.
         *
         * @param runnable      The target that will be executed.
         * @param interval      The interval, in milliseconds, between the start of each execution.
         * @param initialDelay  The delay, in milliseconds, before first execution.
         * @param nextExecution The time of the next scheduled execution of this task, if known.
         * @param lastExecution The last execution of this task, if any.
         * @param enabled       The indicator showing whether the cron task is enabled {@code true} or disabled {@code false}.
         */
        @JsonCreator
        public FixedDelayTask(
                @JsonProperty("runnable") Runnable runnable,
                @JsonProperty("interval") Number interval,
                @JsonProperty("initialDelay") Number initialDelay,
                @JsonProperty("nextExecution") @Nullable NextExecution nextExecution,
                @JsonProperty("lastExecution") @Nullable LastExecution lastExecution,
                @JsonProperty("enabled") boolean enabled) {
            this.runnable = runnable;
            this.interval = interval;
            this.initialDelay = initialDelay;
            this.nextExecution = nextExecution;
            this.lastExecution = lastExecution;
            this.enabled = enabled;
        }

        public Runnable getRunnable() {
            return runnable;
        }

        public Number getInterval() {
            return interval;
        }

        public Number getInitialDelay() {
            return initialDelay;
        }

        @Nullable
        public NextExecution getNextExecution() {
            return nextExecution;
        }

        @Nullable
        public LastExecution getLastExecution() {
            return lastExecution;
        }

        public boolean isEnabled() {
            return enabled;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            FixedDelayTask that = (FixedDelayTask) o;
            return enabled == that.enabled
                    && Objects.equals(runnable, that.runnable)
                    && Objects.equals(interval, that.interval)
                    && Objects.equals(initialDelay, that.initialDelay)
                    && Objects.equals(nextExecution, that.nextExecution)
                    && Objects.equals(lastExecution, that.lastExecution);
        }

        @Override
        public int hashCode() {
            return Objects.hash(runnable, interval, initialDelay, nextExecution, lastExecution, enabled);
        }

        @Override
        public String toString() {
            return "FixedDelayTask{"
                    + "runnable="
                    + runnable
                    + ", interval="
                    + interval
                    + ", initialDelay="
                    + initialDelay
                    + ", nextExecution="
                    + nextExecution
                    + ", lastExecution="
                    + lastExecution
                    + ", enabled="
                    + enabled
                    + '}';
        }
    }

    /**
     * DTO representing the interval between task executions, measured from the start of the previous task execution.
     *
     * @author Sergey Cherkasov
     */
    public static final class FixedRateTask {

        private final Runnable runnable;
        private final Number interval;
        private final Number initialDelay;

        @Nullable
        private final NextExecution nextExecution;

        @Nullable
        private final LastExecution lastExecution;

        private final boolean enabled;

        /**
         * Creates a new FixedRateTask.
         *
         * @param runnable      The target that will be executed.
         * @param interval      The interval, in milliseconds, between the start of each execution.
         * @param initialDelay  The delay, in milliseconds, before first execution.
         * @param nextExecution The time of the next scheduled execution of this task, if known.
         * @param lastExecution The last execution of this task, if any.
         * @param enabled       The indicator showing whether the cron task is enabled {@code true} or disabled {@code false}.
         */
        @JsonCreator
        public FixedRateTask(
                @JsonProperty("runnable") Runnable runnable,
                @JsonProperty("interval") Number interval,
                @JsonProperty("initialDelay") Number initialDelay,
                @JsonProperty("nextExecution") @Nullable NextExecution nextExecution,
                @JsonProperty("lastExecution") @Nullable LastExecution lastExecution,
                @JsonProperty("enabled") boolean enabled) {
            this.runnable = runnable;
            this.interval = interval;
            this.initialDelay = initialDelay;
            this.nextExecution = nextExecution;
            this.lastExecution = lastExecution;
            this.enabled = enabled;
        }

        public Runnable getRunnable() {
            return runnable;
        }

        public Number getInterval() {
            return interval;
        }

        public Number getInitialDelay() {
            return initialDelay;
        }

        @Nullable
        public NextExecution getNextExecution() {
            return nextExecution;
        }

        @Nullable
        public LastExecution getLastExecution() {
            return lastExecution;
        }

        public boolean isEnabled() {
            return enabled;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            FixedRateTask that = (FixedRateTask) o;
            return enabled == that.enabled
                    && Objects.equals(runnable, that.runnable)
                    && Objects.equals(interval, that.interval)
                    && Objects.equals(initialDelay, that.initialDelay)
                    && Objects.equals(nextExecution, that.nextExecution)
                    && Objects.equals(lastExecution, that.lastExecution);
        }

        @Override
        public int hashCode() {
            return Objects.hash(runnable, interval, initialDelay, nextExecution, lastExecution, enabled);
        }

        @Override
        public String toString() {
            return "FixedRateTask{"
                    + "runnable="
                    + runnable
                    + ", interval="
                    + interval
                    + ", initialDelay="
                    + initialDelay
                    + ", nextExecution="
                    + nextExecution
                    + ", lastExecution="
                    + lastExecution
                    + ", enabled="
                    + enabled
                    + '}';
        }
    }

    /**
     * DTO representing a task with a configured user trigger.
     *
     * @author Sergey Cherkasov
     */
    public static final class CustomTask {

        private final Runnable runnable;
        private final String trigger;

        @Nullable
        private final NextExecution nextExecution;

        @Nullable
        private final LastExecution lastExecution;

        private final boolean enabled;

        /**
         * Creates a new CustomTask.
         *
         * @param runnable      The target that will be executed.
         * @param trigger       The trigger used to execute this task.
         * @param nextExecution The time of the next scheduled execution of this task, if known.
         * @param lastExecution The last execution of this task, if any.
         * @param enabled       The indicator showing whether the cron task is enabled {@code true} or disabled {@code false}.
         */
        @JsonCreator
        public CustomTask(
                @JsonProperty("runnable") Runnable runnable,
                @JsonProperty("trigger") String trigger,
                @JsonProperty("nextExecution") @Nullable NextExecution nextExecution,
                @JsonProperty("lastExecution") @Nullable LastExecution lastExecution,
                @JsonProperty("enabled") boolean enabled) {
            this.runnable = runnable;
            this.trigger = trigger;
            this.nextExecution = nextExecution;
            this.lastExecution = lastExecution;
            this.enabled = enabled;
        }

        public Runnable getRunnable() {
            return runnable;
        }

        public String getTrigger() {
            return trigger;
        }

        @Nullable
        public NextExecution getNextExecution() {
            return nextExecution;
        }

        @Nullable
        public LastExecution getLastExecution() {
            return lastExecution;
        }

        public boolean isEnabled() {
            return enabled;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            CustomTask that = (CustomTask) o;
            return enabled == that.enabled
                    && Objects.equals(runnable, that.runnable)
                    && Objects.equals(trigger, that.trigger)
                    && Objects.equals(nextExecution, that.nextExecution)
                    && Objects.equals(lastExecution, that.lastExecution);
        }

        @Override
        public int hashCode() {
            return Objects.hash(runnable, trigger, nextExecution, lastExecution, enabled);
        }

        @Override
        public String toString() {
            return "CustomTask{"
                    + "runnable="
                    + runnable
                    + ", trigger='"
                    + trigger
                    + '\''
                    + ", nextExecution="
                    + nextExecution
                    + ", lastExecution="
                    + lastExecution
                    + ", enabled="
                    + enabled
                    + '}';
        }
    }

    /**
     * DTO representing the last execution of a task.
     *
     * @author Sergey Cherkasov
     */
    public static final class LastExecution {

        private final String status;
        private final String time;

        @Nullable
        private final Exception exception;

        /**
         * Creates a new LastExecution.
         *
         * @param status    The status of the last execution of a task (STARTED, SUCCESS, ERROR).
         * @param time      The time of the last execution of a task.
         * @param exception The exception that may occur, if any.
         */
        @JsonCreator
        public LastExecution(
                @JsonProperty("status") String status,
                @JsonProperty("time") String time,
                @JsonProperty("exception") @Nullable Exception exception) {
            this.status = status;
            this.time = time;
            this.exception = exception;
        }

        public String getStatus() {
            return status;
        }

        public String getTime() {
            return time;
        }

        @Nullable
        public Exception getException() {
            return exception;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            LastExecution that = (LastExecution) o;
            return Objects.equals(status, that.status)
                    && Objects.equals(time, that.time)
                    && Objects.equals(exception, that.exception);
        }

        @Override
        public int hashCode() {
            return Objects.hash(status, time, exception);
        }

        @Override
        public String toString() {
            return "LastExecution{"
                    + "status='"
                    + status
                    + '\''
                    + ", time='"
                    + time
                    + '\''
                    + ", exception="
                    + exception
                    + '}';
        }

        /**
         * DTO representing a possible exception.
         *
         * @author Sergey Cherkasov
         */
        public static final class Exception {

            private final String type;
            private final String message;

            /**
             * Creates a new Exception.
             *
             * @param type    The type of exception thrown by the task, if any.
             * @param message The message of the exception thrown by the task, if any.
             */
            @JsonCreator
            public Exception(@JsonProperty("type") String type, @JsonProperty("message") String message) {
                this.type = type;
                this.message = message;
            }

            public String getType() {
                return type;
            }

            public String getMessage() {
                return message;
            }

            @Override
            public boolean equals(Object o) {
                if (this == o) {
                    return true;
                }
                if (o == null || getClass() != o.getClass()) {
                    return false;
                }
                Exception exception = (Exception) o;
                return Objects.equals(type, exception.type) && Objects.equals(message, exception.message);
            }

            @Override
            public int hashCode() {
                return Objects.hash(type, message);
            }

            @Override
            public String toString() {
                return "Exception{" + "type='" + type + '\'' + ", message='" + message + '\'' + '}';
            }
        }
    }

    /**
     * DTO that contains the next planned execution time of task.
     *
     * @author Sergey Cherkasov
     */
    public static final class NextExecution {

        private final String time;

        @JsonCreator
        public NextExecution(@JsonProperty("time") String time) {
            this.time = time;
        }

        public String getTime() {
            return time;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            NextExecution that = (NextExecution) o;
            return Objects.equals(time, that.time);
        }

        @Override
        public int hashCode() {
            return Objects.hash(time);
        }

        @Override
        public String toString() {
            return "NextExecution{" + "time='" + time + '\'' + '}';
        }
    }

    /**
     * DTO that contains the target that will be executed.
     *
     * @author Sergey Cherkasov
     */
    public static final class Runnable {

        private final String target;

        @JsonCreator
        public Runnable(@JsonProperty("target") String target) {
            this.target = target;
        }

        public String getTarget() {
            return target;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            Runnable runnable = (Runnable) o;
            return Objects.equals(target, runnable.target);
        }

        @Override
        public int hashCode() {
            return Objects.hash(target);
        }

        @Override
        public String toString() {
            return "Runnable{" + "target='" + target + '\'' + '}';
        }
    }
}
