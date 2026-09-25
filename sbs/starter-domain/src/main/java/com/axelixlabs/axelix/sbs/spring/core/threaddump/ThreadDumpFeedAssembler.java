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
package com.axelixlabs.axelix.sbs.spring.core.threaddump;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.jspecify.annotations.Nullable;

import com.axelixlabs.axelix.sbs.spring.core.contract.threaddump.LockInfo;
import com.axelixlabs.axelix.sbs.spring.core.contract.threaddump.MonitorInfo;
import com.axelixlabs.axelix.sbs.spring.core.contract.threaddump.StackTraceElement;
import com.axelixlabs.axelix.sbs.spring.core.contract.threaddump.ThreadDumpFeed;
import com.axelixlabs.axelix.sbs.spring.core.contract.threaddump.ThreadInfo;
import com.axelixlabs.axelix.sbs.spring.core.contract.threaddump.ThreadState;

/**
 * Assembles the {@link ThreadDumpFeed} out of the JMX {@link java.lang.management.ThreadInfo thread infos}.
 *
 * @author Nikita Kirillov
 * @author Mikhail Polivakha
 */
public final class ThreadDumpFeedAssembler {

    private ThreadDumpFeedAssembler() {}

    /**
     * Assemble the {@link ThreadDumpFeed}.
     *
     * @param threadContentionMonitoringEnabled whether the thread contention monitoring is enabled.
     * @param jmxThreads                        the threads, as they were dumped by the JMX.
     * @return the assembled feed.
     */
    public static ThreadDumpFeed assemble(
            boolean threadContentionMonitoringEnabled, java.lang.management.ThreadInfo[] jmxThreads) {
        return new ThreadDumpFeed()
                .threadContentionMonitoringEnabled(threadContentionMonitoringEnabled)
                .threads(Arrays.stream(jmxThreads)
                        .map(ThreadDumpFeedAssembler::toApiThread)
                        .collect(Collectors.toList()));
    }

    private static ThreadInfo toApiThread(java.lang.management.ThreadInfo threadInfo) {
        return new ThreadInfo()
                .threadName(threadInfo.getThreadName())
                .threadId(threadInfo.getThreadId())
                .blockedTime(threadInfo.getBlockedTime())
                .blockedCount(threadInfo.getBlockedCount())
                .waitedTime(threadInfo.getWaitedTime())
                .waitedCount(threadInfo.getWaitedCount())
                .lockInfo(toLockInfo(threadInfo))
                .lockName(threadInfo.getLockName())
                .lockOwnerId(threadInfo.getLockOwnerId())
                .lockOwnerName(threadInfo.getLockOwnerName())
                .daemon(threadInfo.isDaemon())
                .inNative(threadInfo.isInNative())
                .suspended(threadInfo.isSuspended())
                .threadState(toThreadState(threadInfo))
                .priority(threadInfo.getPriority())
                .stackTrace(toStackTrace(threadInfo))
                .lockedMonitors(toLockedMonitors(threadInfo))
                .lockedSynchronizers(toLockedSynchronizers(threadInfo));
    }

    private static List<LockInfo> toLockedSynchronizers(java.lang.management.ThreadInfo threadInfo) {
        if (threadInfo.getLockedSynchronizers() == null) {
            return List.of();
        }
        return Arrays.stream(threadInfo.getLockedSynchronizers())
                .map(it -> new LockInfo().className(it.getClassName()).identityHashCode(it.getIdentityHashCode()))
                .collect(Collectors.toList());
    }

    private static List<MonitorInfo> toLockedMonitors(java.lang.management.ThreadInfo threadInfo) {
        if (threadInfo.getLockedMonitors() == null) {
            return List.of();
        }
        return Arrays.stream(threadInfo.getLockedMonitors())
                .map(it -> new MonitorInfo()
                        .className(it.getClassName())
                        .identityHashCode(it.getIdentityHashCode())
                        .lockedStackDepth(it.getLockedStackDepth())
                        .lockedStackFrame(toStackTraceElement(it.getLockedStackFrame())))
                .collect(Collectors.toList());
    }

    private static List<StackTraceElement> toStackTrace(java.lang.management.ThreadInfo threadInfo) {
        if (threadInfo.getStackTrace() == null) {
            return List.of();
        }
        return Arrays.stream(threadInfo.getStackTrace())
                .map(ThreadDumpFeedAssembler::toStackTraceElement)
                .collect(Collectors.toList());
    }

    private static StackTraceElement toStackTraceElement(java.lang.StackTraceElement element) {
        return new StackTraceElement()
                .classLoaderName(element.getClassLoaderName())
                .className(element.getClassName())
                .fileName(element.getFileName())
                .lineNumber(element.getLineNumber())
                .methodName(element.getMethodName())
                .moduleName(element.getModuleName())
                .moduleVersion(element.getModuleVersion())
                .nativeMethod(element.isNativeMethod());
    }

    private static ThreadState toThreadState(java.lang.management.ThreadInfo threadInfo) {
        switch (threadInfo.getThreadState()) {
            case NEW:
                return ThreadState.NEW;
            case RUNNABLE:
                return ThreadState.RUNNABLE;
            case BLOCKED:
                return ThreadState.BLOCKED;
            case WAITING:
                return ThreadState.WAITING;
            case TIMED_WAITING:
                return ThreadState.TIMED_WAITING;
            case TERMINATED:
                return ThreadState.TERMINATED;
            default:
                throw new IllegalArgumentException("Unknown thread state: " + threadInfo.getThreadState());
        }
    }

    @Nullable
    private static LockInfo toLockInfo(java.lang.management.ThreadInfo threadInfo) {
        if (threadInfo.getLockInfo() == null) {
            return null;
        }
        return new LockInfo()
                .className(threadInfo.getLockInfo().getClassName())
                .identityHashCode(threadInfo.getLockInfo().getIdentityHashCode());
    }
}
