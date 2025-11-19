package com.nucleonforge.axile.common.api;

import java.util.List;

import org.jspecify.annotations.Nullable;

/**
 * The response to /actuator/threaddump endpoint.
 *
 * @apiNote <a href="https://docs.spring.io/spring-boot/api/rest/actuator/threaddump.html">Thread Dump Endpoint</a>
 * @since 18.11.2025
 * @author Nikita Kirillov
 */
public record ThreadDumpFeed(List<ThreadInfo> threads) {

    public record ThreadInfo(
            String threadName,
            long threadId,
            long blockedTime,
            long blockedCount,
            long waitedTime,
            long waitedCount,
            @Nullable LockInfo lockInfo,
            @Nullable String lockName,
            long lockOwnerId,
            @Nullable String lockOwnerName,
            boolean daemon,
            boolean inNative,
            boolean suspended,
            State threadState,
            int priority,
            StackTraceElement[] stackTrace,
            MonitorInfo[] lockedMonitors,
            LockInfo[] lockedSynchronizers) {}

    public record LockInfo(String className, int identityHashCode) {}

    public record MonitorInfo(
            String className, int identityHashCode, int lockedStackDepth, StackTraceElement lockedStackFrame) {}

    public enum State {
        NEW,
        RUNNABLE,
        BLOCKED,
        WAITING,
        TIMED_WAITING,
        TERMINATED;
    }

    public record StackTraceElement(
            @Nullable String classLoaderName,
            String className,
            @Nullable String fileName,
            int lineNumber,
            String methodName,
            @Nullable String moduleName,
            @Nullable String moduleVersion,
            boolean nativeMethod) {}
}
