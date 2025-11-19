package com.nucleonforge.axile.master.api.response;

import java.util.List;

import org.jspecify.annotations.Nullable;

/**
 * The feed of the thread-dump in the application.
 *
 * @since 19.11.2025
 * @author Nikita Kirillov
 */
public record ThreadDumpFeedResponse(List<ThreadInfo> threads) {

    /**
     * @param threadName name of the thread.
     * @param threadId id of the thread.
     * @param blockedTime time in milliseconds that the thread has spent blocked. -1 if thread contention monitoring is disabled.
     * @param blockedCount total number of times that the thread has been blocked.
     * @param waitedTime time in milliseconds that the thread has spent waiting. -1 if thread contention monitoring is disabled.
     * @param waitedCount total number of times that the thread has waited for notification.
     * @param lockInfo object for which the thread is blocked waiting.
     * @param lockName description of the object on which the thread is blocked.
     * @param lockOwnerId id of the thread that owns the object on which the thread is blocked. -1 if the thread is not blocked.
     * @param lockOwnerName name of the thread that owns the object on which the thread is blocked.
     * @param daemon whether the thread is a daemon thread.
     * @param inNative whether the thread is executing native code.
     * @param suspended whether the thread is suspended.
     * @param threadState state of the thread
     * @param priority priority of the thread.
     * @param stackTrace stack trace of the thread.
     * @param lockedMonitors monitors locked by this thread.
     * @param lockedSynchronizers synchronizers locked by this thread.
     */
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

    /**
     * Object for which the thread is blocked waiting.
     *
     * @param className fully qualified class name of the lock object.
     * @param identityHashCode identity hash code of the lock object.
     */
    public record LockInfo(String className, int identityHashCode) {}

    /**
     * Monitors locked by this thread.
     *
     * @param className class name of the lock object.
     * @param identityHashCode identity hash code of the lock object.
     * @param lockedStackDepth stack depth where the monitor was locked.
     * @param lockedStackFrame stack frame that locked the monitor.
     */
    public record MonitorInfo(
            String className, int identityHashCode, int lockedStackDepth, StackTraceElement lockedStackFrame) {}

    /**
     * State of the thread
     */
    public enum State {
        NEW,
        RUNNABLE,
        BLOCKED,
        WAITING,
        TIMED_WAITING,
        TERMINATED
    }

    /**
     * Stack trace of the thread.
     *
     * @param classLoaderName name of the class loader of the class that contains the execution point identified by this entry.
     * @param className name of the class that contains the execution point identified by this entry.
     * @param fileName Name of the source file that contains the execution point identified by this entry.
     * @param lineNumber line number of the execution point identified by this entry. Negative if unknown.
     * @param methodName name of the method.
     * @param moduleName name of the module that contains the execution point identified by this entry.
     * @param moduleVersion version of the module that contains the execution point identified by this entry.
     * @param nativeMethod whether the execution point is a native method.
     */
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
