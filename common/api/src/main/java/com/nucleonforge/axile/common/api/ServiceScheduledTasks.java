package com.nucleonforge.axile.common.api;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.jspecify.annotations.Nullable;

import com.nucleonforge.axile.common.domain.spring.actuator.ActuatorEndpoints;

/**
 * The response of the {@link ActuatorEndpoints#SCHEDULED_TASKS_MANAGEMENT} actuator endpoint provides information about the application’s scheduled tasks.
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
     * DTO representing information about a scheduled cron task.
     *
     * @param delegate  The cron task with precise execution configuration.
     * @param enabled   The indicator showing whether the cron task is enabled {@code true} or disabled {@code false}.
     *
     * @author Sergey Cherkasov
     */
    public record CronTask(@JsonProperty("delegate") Cron delegate, @JsonProperty("enabled") boolean enabled) {

        /**
         * DTO representing a scheduled task with precise execution configuration.
         *
         * @param runnable         The target that will be executed.
         * @param expression       The cron expression (e.g., "0 1 1 5 7 3" or "0 0/15 9-17 ? * MON,WED,FRI" (seconds minutes hours day_of_month month day_of_week))
         * @param nextExecution    The time of the next scheduled execution of this task, if known.
         * @param lastExecution    The last execution of this task, if any.
         *
         * @author Sergey Cherkasov
         */
        public record Cron(
                @JsonProperty("runnable") Runnable runnable,
                @JsonProperty("expression") String expression,
                @JsonProperty("nextExecution") @Nullable NextExecution nextExecution,
                @JsonProperty("lastExecution") @Nullable LastExecution lastExecution) {}
    }

    /**
     * DTO representing information about a scheduled fixedDelay task.
     *
     * @param delegate  The fixedDelay task defines the interval between task executions, counted from the end of the previous task execution.
     * @param enabled   The indicator showing whether the fixedDelay task is enabled {@code true} or disabled {@code false}.
     *
     * @author Sergey Cherkasov
     */
    public record FixedDelayTask(
            @JsonProperty("delegate") FixedDelay delegate, @JsonProperty("enabled") boolean enabled) {

        /**
         * DTO representing the interval between task executions, counted from the end of the previous task execution.
         *
         * @param runnable         The target that will be executed.
         * @param interval         The interval, in milliseconds, between the start of each execution.
         * @param initialDelay     The delay, in milliseconds, before first execution.
         * @param nextExecution    The time of the next scheduled execution of this task, if known.
         * @param lastExecution    The last execution of this task, if any.
         *
         * @author Sergey Cherkasov
         */
        public record FixedDelay(
                @JsonProperty("runnable") Runnable runnable,
                @JsonProperty("interval") Number interval,
                @JsonProperty("initialDelay") Number initialDelay,
                @JsonProperty("nextExecution") @Nullable NextExecution nextExecution,
                @JsonProperty("lastExecution") @Nullable LastExecution lastExecution) {}
    }

    /**
     * DTO representing information about a scheduled fixedRate task.
     *
     * @param delegate  The fixedRate task defines the interval between task executions, measured from the start of the previous task execution.
     * @param enabled   The indicator showing whether the fixedRate task is enabled {@code true} or disabled {@code false}.
     *
     * @author Sergey Cherkasov
     */
    public record FixedRateTask(
            @JsonProperty("delegate") FixedRate delegate, @JsonProperty("enabled") boolean enabled) {

        /**
         * DTO representing the interval between task executions, measured from the start of the previous task execution.
         *
         * @param runnable         The target that will be executed.
         * @param interval         The interval, in milliseconds, between the start of each execution.
         * @param initialDelay     The delay, in milliseconds, before first execution.
         * @param nextExecution    The time of the next scheduled execution of this task, if known.
         * @param lastExecution    The last execution of this task, if any.
         *
         * @author Sergey Cherkasov
         */
        public record FixedRate(
                @JsonProperty("runnable") Runnable runnable,
                @JsonProperty("interval") Number interval,
                @JsonProperty("initialDelay") Number initialDelay,
                @JsonProperty("nextExecution") @Nullable NextExecution nextExecution,
                @JsonProperty("lastExecution") @Nullable LastExecution lastExecution) {}
    }

    /**
     * DTO representing information about a scheduled custom task.
     *
     * @param delegate  The custom task with a configured user trigger.
     * @param enabled   The indicator showing whether the custom task is enabled {@code true} or disabled {@code false}.
     *
     * @author Sergey Cherkasov
     */
    public record CustomTask(@JsonProperty("delegate") Custom delegate, @JsonProperty("enabled") boolean enabled) {
        /**
         * DTO representing a task with a configured user trigger.
         *
         * @param runnable        The target that will be executed.
         * @param trigger         The trigger used to execute this task.
         * @param lastExecution   The last execution of this task, if any.
         *
         * @author Sergey Cherkasov
         */
        public record Custom(
                @JsonProperty("runnable") Runnable runnable,
                @JsonProperty("trigger") String trigger,
                @JsonProperty("lastExecution") @Nullable LastExecution lastExecution) {}
    }

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
