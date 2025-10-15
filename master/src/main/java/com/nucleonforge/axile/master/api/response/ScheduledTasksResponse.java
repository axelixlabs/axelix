package com.nucleonforge.axile.master.api.response;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.jspecify.annotations.Nullable;

/**
 * The profile provides information about the application’s scheduled tasks.
 *
 * @param cron          The list of scheduled cron tasks, if any.
 * @param fixedDelay    The list of scheduled interval with fixed-delay, if any.
 * @param fixedRate     The list of scheduled interval with fixed-rate, if any.
 * @param custom        The list of scheduled tasks with a custom configured user triggers, if any.
 *
 * @author Sergey Cherkasov
 */
public record ScheduledTasksResponse(
        List<Cron> cron, List<FixedDelay> fixedDelay, List<FixedRate> fixedRate, List<Custom> custom) {

    /**
     * The profile representing a scheduled task with precise execution configuration.
     *
     * @param enabled          The indicator showing whether the task is enabled {@code true} or disabled {@code false}.
     * @param runnable         The target that will be executed.
     * @param expression       The cron expression that allows specifying (e.g., "0 1 1 5 7 3" or "0 0/15 9-17 ? * MON,WED,FRI" (seconds minutes hours day_of_month month day_of_week))
     * @param nextExecution    The time of the next scheduled execution of this task, if known.
     * @param lastExecution    The last execution of this task, if any.
     *
     * @author Sergey Cherkasov
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record Cron(
            boolean enabled,
            Runnable runnable,
            String expression,
            @Nullable NextExecution nextExecution,
            @Nullable LastExecution lastExecution) {}

    /**
     * The profile representing the interval between task executions, counted from the end of the previous task execution.
     *
     * @param enabled          The indicator showing whether the task is enabled {@code true} or disabled {@code false}.
     * @param runnable         The target that will be executed.
     * @param interval         The interval, in milliseconds, between the start of each execution.
     * @param initialDelay     The delay, in milliseconds, before first execution.
     * @param nextExecution    The time of the next scheduled execution of this task, if known.
     * @param lastExecution    The last execution of this task, if any.
     *
     * @author Sergey Cherkasov
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record FixedDelay(
            boolean enabled,
            Runnable runnable,
            Number interval,
            Number initialDelay,
            @Nullable NextExecution nextExecution,
            @Nullable LastExecution lastExecution) {}

    /**
     * The profile representing the interval between task executions, measured from the start of the previous task execution.
     *
     * @param enabled          The indicator showing whether the task is enabled {@code true} or disabled {@code false}.
     * @param runnable         The target that will be executed.
     * @param interval         The interval, in milliseconds, between the start of each execution.
     * @param initialDelay     The delay, in milliseconds, before first execution.
     * @param nextExecution    The time of the next scheduled execution of this task, if known.
     * @param lastExecution    The last execution of this task, if any.
     *
     * @author Sergey Cherkasov
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record FixedRate(
            boolean enabled,
            Runnable runnable,
            Number interval,
            Number initialDelay,
            @Nullable NextExecution nextExecution,
            @Nullable LastExecution lastExecution) {}

    /**
     * The profile representing a task with a configured user trigger.
     *
     * @param enabled         The indicator showing whether the task is enabled {@code true} or disabled {@code false}.
     * @param runnable        The target that will be executed.
     * @param trigger         The trigger used to execute this task.
     * @param lastExecution   The last execution of this task, if any.
     *
     * @author Sergey Cherkasov
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record Custom(boolean enabled, Runnable runnable, String trigger, @Nullable LastExecution lastExecution) {}

    /**
     * The profile representing the last execution of the task.
     *
     * @param status      The status of the last execution of a task (STARTED, SUCCESS, ERROR).
     * @param time        The time of the last execution of a task.
     * @param exception   The exception that may occur, if any.
     *
     * @author Sergey Cherkasov
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record LastExecution(String status, String time, @Nullable Exception exception) {

        /**
         * The profile representing a possible exception.
         *
         * @param type      The type of exception thrown by the task, if any.
         * @param message   The message of the exception thrown by the task, if any.
         *
         * @author Sergey Cherkasov
         */
        @JsonInclude(JsonInclude.Include.NON_NULL)
        public record Exception(String type, String message) {}
    }

    /**
     * The profile that contains the next execution time of the task.
     *
     * @param time  The execution time.
     *
     * @author Sergey Cherkasov
     */
    public record NextExecution(String time) {}

    /**
     * The profile that contains the target that will be executed.
     *
     * @param target  The target for execution.
     *
     * @author Sergey Cherkasov
     */
    public record Runnable(String target) {}
}
