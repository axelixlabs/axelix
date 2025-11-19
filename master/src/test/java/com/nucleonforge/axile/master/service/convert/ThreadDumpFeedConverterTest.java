package com.nucleonforge.axile.master.service.convert;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.nucleonforge.axile.common.api.ThreadDumpFeed;
import com.nucleonforge.axile.master.api.response.ThreadDumpFeedResponse;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link ThreadDumpFeedConverter}.
 *
 * @since 19.11.2025
 * @author Nikita Kirillov
 */
class ThreadDumpFeedConverterTest {

    private final ThreadDumpFeedConverter subject = new ThreadDumpFeedConverter();

    @Test
    void testConvertHappyPath() {
        ThreadDumpFeedResponse threadDumpFeedResponse = subject.convertInternal(getThreadDumpFeed());

        assertThat(threadDumpFeedResponse.threads()).hasSize(1);

        ThreadDumpFeedResponse.ThreadInfo threadInfo =
                threadDumpFeedResponse.threads().get(0);
        assertThat(threadInfo.threadName()).isEqualTo("Test worker");
        assertThat(threadInfo.threadId()).isEqualTo(1L);
        assertThat(threadInfo.blockedTime()).isEqualTo(-1L);
        assertThat(threadInfo.blockedCount()).isEqualTo(100L);
        assertThat(threadInfo.waitedTime()).isEqualTo(1L);
        assertThat(threadInfo.waitedCount()).isEqualTo(2L);
        assertThat(threadInfo.lockName()).isEqualTo("java.lang.ref.ReferenceQueue$Lock@60ffdc9f");
        assertThat(threadInfo.lockOwnerId()).isEqualTo(-1L);
        assertThat(threadInfo.lockOwnerName()).isNull();
        assertThat(threadInfo.daemon()).isEqualTo(true);
        assertThat(threadInfo.inNative()).isEqualTo(false);
        assertThat(threadInfo.suspended()).isEqualTo(true);
        assertThat(threadInfo.threadState()).isEqualTo(ThreadDumpFeedResponse.State.NEW);
        assertThat(threadInfo.priority()).isEqualTo(9);

        assertThat(threadInfo.lockInfo()).isNotNull().satisfies(lockInfo -> {
            assertThat(lockInfo.className()).isEqualTo("java.lang.ref.ReferenceQueue$Lock");
            assertThat(lockInfo.identityHashCode()).isEqualTo(1627380895);
        });

        assertThat(threadInfo.stackTrace()).hasSize(1).satisfies(stackTraceElements -> {
            ThreadDumpFeedResponse.StackTraceElement stackTraceElement = stackTraceElements[0];
            assertThat(stackTraceElement.classLoaderName()).isEqualTo("app");
            assertThat(stackTraceElement.className()).isEqualTo("java.lang.ref.Finalizer$FinalizerThread");
            assertThat(stackTraceElement.fileName()).isEqualTo("Finalizer.java");
            assertThat(stackTraceElement.lineNumber()).isEqualTo(172);
            assertThat(stackTraceElement.methodName()).isEqualTo("run");
            assertThat(stackTraceElement.moduleName()).isEqualTo("java.base");
            assertThat(stackTraceElement.moduleVersion()).isEqualTo("17.0.17");
            assertThat(stackTraceElement.nativeMethod()).isEqualTo(false);
        });

        assertThat(threadInfo.lockedMonitors()).hasSize(1).satisfies(monitorInfos -> {
            ThreadDumpFeedResponse.MonitorInfo monitorInfo = monitorInfos[0];
            assertThat(monitorInfo.className()).isEqualTo("java.io.InputStreamReader");
            assertThat(monitorInfo.identityHashCode()).isEqualTo(2012856739);
            assertThat(monitorInfo.lockedStackDepth()).isEqualTo(12);
            assertThat(monitorInfo.lockedStackFrame()).isNotNull();
            assertThat(monitorInfo.lockedStackFrame().classLoaderName()).isEqualTo("app");
            assertThat(monitorInfo.lockedStackFrame().className()).isEqualTo("java.lang.ref.Finalizer$FinalizerThread");
            assertThat(monitorInfo.lockedStackFrame().fileName()).isEqualTo("Finalizer.java");
            assertThat(monitorInfo.lockedStackFrame().lineNumber()).isEqualTo(172);
            assertThat(monitorInfo.lockedStackFrame().methodName()).isEqualTo("run");
            assertThat(monitorInfo.lockedStackFrame().moduleName()).isEqualTo("java.base");
            assertThat(monitorInfo.lockedStackFrame().moduleVersion()).isEqualTo("17.0.17");
            assertThat(monitorInfo.lockedStackFrame().nativeMethod()).isEqualTo(false);
        });

        assertThat(threadInfo.lockedSynchronizers()).hasSize(1).satisfies(lockInfos -> {
            ThreadDumpFeedResponse.LockInfo lockInfo = lockInfos[0];
            assertThat(lockInfo.className()).isEqualTo("java.lang.ref.ReferenceQueue$Lock");
            assertThat(lockInfo.identityHashCode()).isEqualTo(1627380895);
        });
    }

    private static ThreadDumpFeed getThreadDumpFeed() {
        return new ThreadDumpFeed(List.of(getThreadInfo()));
    }

    private static ThreadDumpFeed.ThreadInfo getThreadInfo() {
        return new ThreadDumpFeed.ThreadInfo(
                "Test worker",
                1L,
                -1L,
                100L,
                1L,
                2L,
                getLockInfo(),
                "java.lang.ref.ReferenceQueue$Lock@60ffdc9f",
                -1L,
                null,
                true,
                false,
                true,
                ThreadDumpFeed.State.NEW,
                9,
                new ThreadDumpFeed.StackTraceElement[] {getStackTraceElement()},
                new ThreadDumpFeed.MonitorInfo[] {getMonitorInfo()},
                new ThreadDumpFeed.LockInfo[] {getLockInfo()});
    }

    private static ThreadDumpFeed.LockInfo getLockInfo() {
        return new ThreadDumpFeed.LockInfo("java.lang.ref.ReferenceQueue$Lock", 1627380895);
    }

    private static ThreadDumpFeed.StackTraceElement getStackTraceElement() {
        return new ThreadDumpFeed.StackTraceElement(
                "app",
                "java.lang.ref.Finalizer$FinalizerThread",
                "Finalizer.java",
                172,
                "run",
                "java.base",
                "17.0.17",
                false);
    }

    private static ThreadDumpFeed.MonitorInfo getMonitorInfo() {
        return new ThreadDumpFeed.MonitorInfo("java.io.InputStreamReader", 2012856739, 12, getStackTraceElement());
    }
}
