export enum EStatisticType {
    /**
     * The sum of the amounts recorded.
     */
    TOTAL,

    /**
     * The sum of the times recorded. Reported in the monitoring system's base unit of
     * time
     */
    TOTAL_TIME,

    /**
     * Rate per second for calls.
     */
    COUNT,

    /**
     * The maximum value recorded. When this represents a time, it is reported in the
     * monitoring system's base unit of time.
     */
    MAX,

    /**
     * Instantaneous, current value of the metric.
     */
    VALUE,

    /**
     * Undetermined.
     */
    UNKNOWN,

    /**
     * Number of currently active tasks for a long task timer.
     */
    ACTIVE_TASKS,

    /**
     * Duration of a running task in a long task timer. Always reported in the monitoring
     * system's base unit of time.
     */
    DURATION,
}
