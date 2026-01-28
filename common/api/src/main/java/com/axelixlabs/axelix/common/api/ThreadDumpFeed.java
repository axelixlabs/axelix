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

import java.util.Arrays;
import java.util.List;

import org.jspecify.annotations.Nullable;

/**
 * The response to thread dump endpoint.
 *
 * @param threadContentionMonitoringEnabled whether the thread contention monitoring is enabled.
 * @param threads thread dump itself.
 *
 * @apiNote <a href="https://docs.spring.io/spring-boot/api/rest/actuator/threaddump.html">Thread Dump Endpoint</a>
 * @since 18.11.2025
 * @author Nikita Kirillov
 * @author Mikhail Polivakha
 */
public record ThreadDumpFeed(boolean threadContentionMonitoringEnabled, List<ThreadInfo> threads) {

    public ThreadDumpFeed(boolean threadContentionMonitoringEnabled, java.lang.management.ThreadInfo[] jmxThreads) {
        this(
                threadContentionMonitoringEnabled,
                Arrays.stream(jmxThreads).map(ThreadDumpFeed::toApiThread).toList());
    }

    private static ThreadInfo toApiThread(java.lang.management.ThreadInfo threadInfo) {
        return new ThreadInfo(
                threadInfo.getThreadName(),
                threadInfo.getThreadId(),
                threadInfo.getBlockedTime(),
                threadInfo.getBlockedCount(),
                threadInfo.getWaitedTime(),
                threadInfo.getWaitedCount(),
                toLockInfo(threadInfo),
                threadInfo.getLockName(),
                threadInfo.getLockOwnerId(),
                threadInfo.getLockOwnerName(),
                threadInfo.isDaemon(),
                threadInfo.isInNative(),
                threadInfo.isSuspended(),
                toThreadState(threadInfo),
                threadInfo.getPriority(),
                toStackTrace(threadInfo),
                toLockedMonitors(threadInfo),
                toLockedSynchronizers(threadInfo));
    }

    private static LockInfo[] toLockedSynchronizers(java.lang.management.ThreadInfo threadInfo) {
        if (threadInfo.getLockedSynchronizers() != null) {
            return Arrays.stream(threadInfo.getLockedSynchronizers())
                    .map(it -> new LockInfo(it.getClassName(), it.getIdentityHashCode()))
                    .toArray(LockInfo[]::new);
        } else {
            return new LockInfo[0];
        }
    }

    private static MonitorInfo[] toLockedMonitors(java.lang.management.ThreadInfo threadInfo) {
        if (threadInfo.getLockedMonitors() != null) {
            return Arrays.stream(threadInfo.getLockedMonitors())
                    .map(it -> new MonitorInfo(
                            it.getClassName(),
                            it.getIdentityHashCode(),
                            it.getLockedStackDepth(),
                            new StackTraceElement(
                                    it.getLockedStackFrame().getClassLoaderName(),
                                    it.getLockedStackFrame().getClassName(),
                                    it.getLockedStackFrame().getFileName(),
                                    it.getLockedStackFrame().getLineNumber(),
                                    it.getLockedStackFrame().getMethodName(),
                                    it.getLockedStackFrame().getModuleName(),
                                    it.getLockedStackFrame().getModuleVersion(),
                                    it.getLockedStackFrame().isNativeMethod())))
                    .toArray(MonitorInfo[]::new);
        } else {
            return new MonitorInfo[0];
        }
    }

    private static StackTraceElement[] toStackTrace(java.lang.management.ThreadInfo threadInfo) {
        if (threadInfo.getStackTrace() != null) {
            return Arrays.stream(threadInfo.getStackTrace())
                    .map(it -> new StackTraceElement(
                            it.getClassLoaderName(),
                            it.getClassName(),
                            it.getFileName(),
                            it.getLineNumber(),
                            it.getMethodName(),
                            it.getModuleName(),
                            it.getModuleVersion(),
                            it.isNativeMethod()))
                    .toArray(StackTraceElement[]::new);
        } else {
            return new StackTraceElement[0];
        }
    }

    private static State toThreadState(java.lang.management.ThreadInfo threadInfo) {
        return switch (threadInfo.getThreadState()) {
            case NEW -> State.NEW;
            case RUNNABLE -> State.RUNNABLE;
            case BLOCKED -> State.BLOCKED;
            case WAITING -> State.WAITING;
            case TIMED_WAITING -> State.TIMED_WAITING;
            case TERMINATED -> State.TERMINATED;
        };
    }

    @Nullable
    private static LockInfo toLockInfo(java.lang.management.ThreadInfo threadInfo) {
        if (threadInfo.getLockInfo() != null) {
            return new LockInfo(
                    threadInfo.getLockInfo().getClassName(),
                    threadInfo.getLockInfo().getIdentityHashCode());
        } else {
            return null;
        }
    }

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
