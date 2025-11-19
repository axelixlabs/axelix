package com.nucleonforge.axile.master.service.serde;

import java.nio.charset.StandardCharsets;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import com.nucleonforge.axile.common.api.ThreadDumpFeed;
import com.nucleonforge.axile.common.api.ThreadDumpFeed.StackTraceElement;
import com.nucleonforge.axile.common.api.ThreadDumpFeed.State;
import com.nucleonforge.axile.common.api.ThreadDumpFeed.ThreadInfo;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link ThreadDumpJacksonMessageDeserializationStrategy}.
 *
 * @since 19.11.2025
 * @author Nikita Kirillov
 */
class ThreadDumpJacksonMessageDeserializationStrategyTest {

    private final ThreadDumpJacksonMessageDeserializationStrategy subject =
            new ThreadDumpJacksonMessageDeserializationStrategy(new ObjectMapper());

    @Test
    void shouldDeserializeThreadDumpFeed() {
        // language=json
        String jsonResponse =
                """
    {
       "threads" : [ {
         "threadName" : "Test worker",
         "threadId" : 1,
         "blockedTime" : -1,
         "blockedCount" : 37,
         "waitedTime" : -1,
         "waitedCount" : 109,
         "lockOwnerId" : -1,
         "daemon" : false,
         "inNative" : false,
         "suspended" : false,
         "threadState" : "RUNNABLE",
         "priority" : 5,
         "stackTrace" : [ {
           "moduleName" : "java.management",
           "moduleVersion" : "17.0.17",
           "methodName" : "dumpThreads0",
           "fileName" : "ThreadImpl.java",
           "lineNumber" : -2,
           "nativeMethod" : true,
           "className" : "sun.management.ThreadImpl"
         }, {
           "moduleName" : "java.management",
           "moduleVersion" : "17.0.17",
           "methodName" : "dumpAllThreads",
           "fileName" : "ThreadImpl.java",
           "lineNumber" : 528,
           "nativeMethod" : false,
           "className" : "sun.management.ThreadImpl"
         } ],
         "lockedMonitors" : [ ],
         "lockedSynchronizers" : [ ]
       }, {
         "threadName" : "Finalizer",
         "threadId" : 3,
         "blockedTime" : -1,
         "blockedCount" : 0,
         "waitedTime" : -1,
         "waitedCount" : 1,
         "lockName" : "java.lang.ref.ReferenceQueue$Lock@60ffdc9f",
         "lockOwnerId" : -1,
         "daemon" : true,
         "inNative" : false,
         "suspended" : false,
         "threadState" : "WAITING",
         "priority" : 8,
         "stackTrace" : [ {
           "moduleName" : "java.base",
           "moduleVersion" : "17.0.17",
           "methodName" : "wait",
           "lineNumber" : -2,
           "nativeMethod" : true,
           "className" : "java.lang.Object"
         }, {
           "moduleName" : "java.base",
           "moduleVersion" : "17.0.17",
           "methodName" : "run",
           "fileName" : "Finalizer.java",
           "lineNumber" : 172,
           "nativeMethod" : false,
           "className" : "java.lang.ref.Finalizer$FinalizerThread"
         } ],
         "lockedMonitors" : [ ],
         "lockedSynchronizers" : [ ],
         "lockInfo" : {
           "className" : "java.lang.ref.ReferenceQueue$Lock",
           "identityHashCode" : 1627380895
         }
       } ]
    }
    """;

        ThreadDumpFeed threadDumpFeed = subject.deserialize(jsonResponse.getBytes(StandardCharsets.UTF_8));

        assertThat(threadDumpFeed.threads()).hasSize(2);

        ThreadInfo firstThread = threadDumpFeed.threads().get(0);
        assertThat(firstThread.threadName()).isEqualTo("Test worker");
        assertThat(firstThread.threadId()).isEqualTo(1);
        assertThat(firstThread.blockedTime()).isEqualTo(-1);
        assertThat(firstThread.blockedCount()).isEqualTo(37);
        assertThat(firstThread.waitedTime()).isEqualTo(-1);
        assertThat(firstThread.waitedCount()).isEqualTo(109);
        assertThat(firstThread.lockOwnerId()).isEqualTo(-1);
        assertThat(firstThread.daemon()).isFalse();
        assertThat(firstThread.inNative()).isFalse();
        assertThat(firstThread.suspended()).isFalse();
        assertThat(firstThread.threadState()).isEqualTo(State.RUNNABLE);
        assertThat(firstThread.priority()).isEqualTo(5);
        assertThat(firstThread.lockInfo()).isNull();
        assertThat(firstThread.lockName()).isNull();
        assertThat(firstThread.lockOwnerName()).isNull();

        assertThat(firstThread.stackTrace()).hasSize(2);
        StackTraceElement firstStackTrace = firstThread.stackTrace()[0];
        assertThat(firstStackTrace.moduleName()).isEqualTo("java.management");
        assertThat(firstStackTrace.moduleVersion()).isEqualTo("17.0.17");
        assertThat(firstStackTrace.methodName()).isEqualTo("dumpThreads0");
        assertThat(firstStackTrace.fileName()).isEqualTo("ThreadImpl.java");
        assertThat(firstStackTrace.lineNumber()).isEqualTo(-2);
        assertThat(firstStackTrace.nativeMethod()).isTrue();
        assertThat(firstStackTrace.className()).isEqualTo("sun.management.ThreadImpl");

        StackTraceElement secondStackTrace = firstThread.stackTrace()[1];
        assertThat(secondStackTrace.methodName()).isEqualTo("dumpAllThreads");
        assertThat(secondStackTrace.lineNumber()).isEqualTo(528);
        assertThat(secondStackTrace.nativeMethod()).isFalse();

        assertThat(firstThread.lockedMonitors()).isEmpty();
        assertThat(firstThread.lockedSynchronizers()).isEmpty();

        ThreadInfo secondThread = threadDumpFeed.threads().get(1);
        assertThat(secondThread.threadName()).isEqualTo("Finalizer");
        assertThat(secondThread.threadId()).isEqualTo(3);
        assertThat(secondThread.blockedTime()).isEqualTo(-1);
        assertThat(secondThread.blockedCount()).isEqualTo(0);
        assertThat(secondThread.waitedTime()).isEqualTo(-1);
        assertThat(secondThread.waitedCount()).isEqualTo(1);
        assertThat(secondThread.lockName()).isEqualTo("java.lang.ref.ReferenceQueue$Lock@60ffdc9f");
        assertThat(secondThread.lockOwnerId()).isEqualTo(-1);
        assertThat(secondThread.daemon()).isTrue();
        assertThat(secondThread.inNative()).isFalse();
        assertThat(secondThread.suspended()).isFalse();
        assertThat(secondThread.threadState()).isEqualTo(State.WAITING);
        assertThat(secondThread.priority()).isEqualTo(8);

        assertThat(secondThread.lockInfo()).isNotNull();
        assertThat(secondThread.lockInfo().className()).isEqualTo("java.lang.ref.ReferenceQueue$Lock");
        assertThat(secondThread.lockInfo().identityHashCode()).isEqualTo(1627380895);

        assertThat(secondThread.stackTrace()).hasSize(2);
        StackTraceElement secondThreadFirstFrame = secondThread.stackTrace()[0];
        assertThat(secondThreadFirstFrame.moduleName()).isEqualTo("java.base");
        assertThat(secondThreadFirstFrame.methodName()).isEqualTo("wait");
        assertThat(secondThreadFirstFrame.nativeMethod()).isTrue();
        assertThat(secondThreadFirstFrame.className()).isEqualTo("java.lang.Object");

        StackTraceElement secondThreadSecondFrame = secondThread.stackTrace()[1];
        assertThat(secondThreadSecondFrame.fileName()).isEqualTo("Finalizer.java");
        assertThat(secondThreadSecondFrame.lineNumber()).isEqualTo(172);
        assertThat(secondThreadSecondFrame.methodName()).isEqualTo("run");
        assertThat(secondThreadSecondFrame.nativeMethod()).isFalse();
        assertThat(secondThreadSecondFrame.className()).isEqualTo("java.lang.ref.Finalizer$FinalizerThread");

        assertThat(secondThread.lockedMonitors()).isEmpty();
        assertThat(secondThread.lockedSynchronizers()).isEmpty();
    }
}
