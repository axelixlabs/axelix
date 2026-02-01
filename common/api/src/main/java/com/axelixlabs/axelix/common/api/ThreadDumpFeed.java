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
import java.util.Objects;
import java.util.stream.Collectors;

import org.jspecify.annotations.Nullable;

/**
 * The response to thread dump endpoint.
 *
 * @apiNote <a href="https://docs.spring.io/spring-boot/api/rest/actuator/threaddump.html">Thread Dump Endpoint</a>
 * @since 18.11.2025
 * @author Nikita Kirillov
 * @author Mikhail Polivakha
 */
public final class ThreadDumpFeed {

    private final boolean threadContentionMonitoringEnabled;
    private final List<ThreadInfo> threads;

    /**
     * Creates a new ThreadDumpFeed.
     *
     * @param threadContentionMonitoringEnabled whether the thread contention monitoring is enabled.
     * @param threads                           thread dump itself.
     */
    public ThreadDumpFeed(boolean threadContentionMonitoringEnabled, List<ThreadInfo> threads) {
        this.threadContentionMonitoringEnabled = threadContentionMonitoringEnabled;
        this.threads = threads;
    }

    public ThreadDumpFeed(boolean threadContentionMonitoringEnabled, java.lang.management.ThreadInfo[] jmxThreads) {
        this(
                threadContentionMonitoringEnabled,
                Arrays.stream(jmxThreads).map(ThreadDumpFeed::toApiThread).collect(Collectors.toList()));
    }

    public boolean threadContentionMonitoringEnabled() {
        return threadContentionMonitoringEnabled;
    }

    public List<ThreadInfo> threads() {
        return threads;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ThreadDumpFeed that = (ThreadDumpFeed) o;
        return threadContentionMonitoringEnabled == that.threadContentionMonitoringEnabled
                && Objects.equals(threads, that.threads);
    }

    @Override
    public int hashCode() {
        return Objects.hash(threadContentionMonitoringEnabled, threads);
    }

    @Override
    public String toString() {
        return "ThreadDumpFeed{"
                + "threadContentionMonitoringEnabled="
                + threadContentionMonitoringEnabled
                + ", threads="
                + threads
                + '}';
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
        switch (threadInfo.getThreadState()) {
            case NEW:
                return State.NEW;
            case RUNNABLE:
                return State.RUNNABLE;
            case BLOCKED:
                return State.BLOCKED;
            case WAITING:
                return State.WAITING;
            case TIMED_WAITING:
                return State.TIMED_WAITING;
            case TERMINATED:
                return State.TERMINATED;
            default:
                throw new IllegalArgumentException("Unknown thread state: " + threadInfo.getThreadState());
        }
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

    public static final class ThreadInfo {

        private final String threadName;
        private final long threadId;
        private final long blockedTime;
        private final long blockedCount;
        private final long waitedTime;
        private final long waitedCount;

        @Nullable
        private final LockInfo lockInfo;

        @Nullable
        private final String lockName;

        private final long lockOwnerId;

        @Nullable
        private final String lockOwnerName;

        private final boolean daemon;
        private final boolean inNative;
        private final boolean suspended;
        private final State threadState;
        private final int priority;
        private final StackTraceElement[] stackTrace;
        private final MonitorInfo[] lockedMonitors;
        private final LockInfo[] lockedSynchronizers;

        public ThreadInfo(
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
                LockInfo[] lockedSynchronizers) {
            this.threadName = threadName;
            this.threadId = threadId;
            this.blockedTime = blockedTime;
            this.blockedCount = blockedCount;
            this.waitedTime = waitedTime;
            this.waitedCount = waitedCount;
            this.lockInfo = lockInfo;
            this.lockName = lockName;
            this.lockOwnerId = lockOwnerId;
            this.lockOwnerName = lockOwnerName;
            this.daemon = daemon;
            this.inNative = inNative;
            this.suspended = suspended;
            this.threadState = threadState;
            this.priority = priority;
            this.stackTrace = stackTrace;
            this.lockedMonitors = lockedMonitors;
            this.lockedSynchronizers = lockedSynchronizers;
        }

        public String threadName() {
            return threadName;
        }

        public long threadId() {
            return threadId;
        }

        public long blockedTime() {
            return blockedTime;
        }

        public long blockedCount() {
            return blockedCount;
        }

        public long waitedTime() {
            return waitedTime;
        }

        public long waitedCount() {
            return waitedCount;
        }

        @Nullable
        public LockInfo lockInfo() {
            return lockInfo;
        }

        @Nullable
        public String lockName() {
            return lockName;
        }

        public long lockOwnerId() {
            return lockOwnerId;
        }

        @Nullable
        public String lockOwnerName() {
            return lockOwnerName;
        }

        public boolean daemon() {
            return daemon;
        }

        public boolean inNative() {
            return inNative;
        }

        public boolean suspended() {
            return suspended;
        }

        public State threadState() {
            return threadState;
        }

        public int priority() {
            return priority;
        }

        public StackTraceElement[] stackTrace() {
            return stackTrace;
        }

        public MonitorInfo[] lockedMonitors() {
            return lockedMonitors;
        }

        public LockInfo[] lockedSynchronizers() {
            return lockedSynchronizers;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ThreadInfo that = (ThreadInfo) o;
            return threadId == that.threadId
                    && blockedTime == that.blockedTime
                    && blockedCount == that.blockedCount
                    && waitedTime == that.waitedTime
                    && waitedCount == that.waitedCount
                    && lockOwnerId == that.lockOwnerId
                    && daemon == that.daemon
                    && inNative == that.inNative
                    && suspended == that.suspended
                    && priority == that.priority
                    && Objects.equals(threadName, that.threadName)
                    && Objects.equals(lockInfo, that.lockInfo)
                    && Objects.equals(lockName, that.lockName)
                    && Objects.equals(lockOwnerName, that.lockOwnerName)
                    && threadState == that.threadState
                    && Arrays.equals(stackTrace, that.stackTrace)
                    && Arrays.equals(lockedMonitors, that.lockedMonitors)
                    && Arrays.equals(lockedSynchronizers, that.lockedSynchronizers);
        }

        @Override
        public int hashCode() {
            int result = Objects.hash(
                    threadName,
                    threadId,
                    blockedTime,
                    blockedCount,
                    waitedTime,
                    waitedCount,
                    lockInfo,
                    lockName,
                    lockOwnerId,
                    lockOwnerName,
                    daemon,
                    inNative,
                    suspended,
                    threadState,
                    priority);
            result = 31 * result + Arrays.hashCode(stackTrace);
            result = 31 * result + Arrays.hashCode(lockedMonitors);
            result = 31 * result + Arrays.hashCode(lockedSynchronizers);
            return result;
        }

        @Override
        public String toString() {
            return "ThreadInfo{"
                    + "threadName='"
                    + threadName
                    + '\''
                    + ", threadId="
                    + threadId
                    + ", blockedTime="
                    + blockedTime
                    + ", blockedCount="
                    + blockedCount
                    + ", waitedTime="
                    + waitedTime
                    + ", waitedCount="
                    + waitedCount
                    + ", lockInfo="
                    + lockInfo
                    + ", lockName='"
                    + lockName
                    + '\''
                    + ", lockOwnerId="
                    + lockOwnerId
                    + ", lockOwnerName='"
                    + lockOwnerName
                    + '\''
                    + ", daemon="
                    + daemon
                    + ", inNative="
                    + inNative
                    + ", suspended="
                    + suspended
                    + ", threadState="
                    + threadState
                    + ", priority="
                    + priority
                    + ", stackTrace="
                    + Arrays.toString(stackTrace)
                    + ", lockedMonitors="
                    + Arrays.toString(lockedMonitors)
                    + ", lockedSynchronizers="
                    + Arrays.toString(lockedSynchronizers)
                    + '}';
        }
    }

    public static final class LockInfo {

        private final String className;
        private final int identityHashCode;

        public LockInfo(String className, int identityHashCode) {
            this.className = className;
            this.identityHashCode = identityHashCode;
        }

        public String className() {
            return className;
        }

        public int identityHashCode() {
            return identityHashCode;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            LockInfo lockInfo = (LockInfo) o;
            return identityHashCode == lockInfo.identityHashCode && Objects.equals(className, lockInfo.className);
        }

        @Override
        public int hashCode() {
            return Objects.hash(className, identityHashCode);
        }

        @Override
        public String toString() {
            return "LockInfo{" + "className='" + className + '\'' + ", identityHashCode=" + identityHashCode + '}';
        }
    }

    public static final class MonitorInfo {

        private final String className;
        private final int identityHashCode;
        private final int lockedStackDepth;
        private final StackTraceElement lockedStackFrame;

        public MonitorInfo(
                String className, int identityHashCode, int lockedStackDepth, StackTraceElement lockedStackFrame) {
            this.className = className;
            this.identityHashCode = identityHashCode;
            this.lockedStackDepth = lockedStackDepth;
            this.lockedStackFrame = lockedStackFrame;
        }

        public String className() {
            return className;
        }

        public int identityHashCode() {
            return identityHashCode;
        }

        public int lockedStackDepth() {
            return lockedStackDepth;
        }

        public StackTraceElement lockedStackFrame() {
            return lockedStackFrame;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            MonitorInfo that = (MonitorInfo) o;
            return identityHashCode == that.identityHashCode
                    && lockedStackDepth == that.lockedStackDepth
                    && Objects.equals(className, that.className)
                    && Objects.equals(lockedStackFrame, that.lockedStackFrame);
        }

        @Override
        public int hashCode() {
            return Objects.hash(className, identityHashCode, lockedStackDepth, lockedStackFrame);
        }

        @Override
        public String toString() {
            return "MonitorInfo{"
                    + "className='"
                    + className
                    + '\''
                    + ", identityHashCode="
                    + identityHashCode
                    + ", lockedStackDepth="
                    + lockedStackDepth
                    + ", lockedStackFrame="
                    + lockedStackFrame
                    + '}';
        }
    }

    public enum State {
        NEW,
        RUNNABLE,
        BLOCKED,
        WAITING,
        TIMED_WAITING,
        TERMINATED;
    }

    public static final class StackTraceElement {

        @Nullable
        private final String classLoaderName;

        private final String className;

        @Nullable
        private final String fileName;

        private final int lineNumber;
        private final String methodName;

        @Nullable
        private final String moduleName;

        @Nullable
        private final String moduleVersion;

        private final boolean nativeMethod;

        public StackTraceElement(
                @Nullable String classLoaderName,
                String className,
                @Nullable String fileName,
                int lineNumber,
                String methodName,
                @Nullable String moduleName,
                @Nullable String moduleVersion,
                boolean nativeMethod) {
            this.classLoaderName = classLoaderName;
            this.className = className;
            this.fileName = fileName;
            this.lineNumber = lineNumber;
            this.methodName = methodName;
            this.moduleName = moduleName;
            this.moduleVersion = moduleVersion;
            this.nativeMethod = nativeMethod;
        }

        @Nullable
        public String classLoaderName() {
            return classLoaderName;
        }

        public String className() {
            return className;
        }

        @Nullable
        public String fileName() {
            return fileName;
        }

        public int lineNumber() {
            return lineNumber;
        }

        public String methodName() {
            return methodName;
        }

        @Nullable
        public String moduleName() {
            return moduleName;
        }

        @Nullable
        public String moduleVersion() {
            return moduleVersion;
        }

        public boolean nativeMethod() {
            return nativeMethod;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            StackTraceElement that = (StackTraceElement) o;
            return lineNumber == that.lineNumber
                    && nativeMethod == that.nativeMethod
                    && Objects.equals(classLoaderName, that.classLoaderName)
                    && Objects.equals(className, that.className)
                    && Objects.equals(fileName, that.fileName)
                    && Objects.equals(methodName, that.methodName)
                    && Objects.equals(moduleName, that.moduleName)
                    && Objects.equals(moduleVersion, that.moduleVersion);
        }

        @Override
        public int hashCode() {
            return Objects.hash(
                    classLoaderName,
                    className,
                    fileName,
                    lineNumber,
                    methodName,
                    moduleName,
                    moduleVersion,
                    nativeMethod);
        }

        @Override
        public String toString() {
            return "StackTraceElement{"
                    + "classLoaderName='"
                    + classLoaderName
                    + '\''
                    + ", className='"
                    + className
                    + '\''
                    + ", fileName='"
                    + fileName
                    + '\''
                    + ", lineNumber="
                    + lineNumber
                    + ", methodName='"
                    + methodName
                    + '\''
                    + ", moduleName='"
                    + moduleName
                    + '\''
                    + ", moduleVersion='"
                    + moduleVersion
                    + '\''
                    + ", nativeMethod="
                    + nativeMethod
                    + '}';
        }
    }
}
