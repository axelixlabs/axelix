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
package com.axelixlabs.axelix.master.service.serde;

import java.nio.charset.StandardCharsets;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import com.axelixlabs.axelix.common.api.ThreadDumpFeed;
import com.axelixlabs.axelix.common.api.ThreadDumpFeed.StackTraceElement;
import com.axelixlabs.axelix.common.api.ThreadDumpFeed.State;
import com.axelixlabs.axelix.common.api.ThreadDumpFeed.ThreadInfo;

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

        assertThat(threadDumpFeed.getThreads()).hasSize(2);

        ThreadInfo firstThread = threadDumpFeed.getThreads().get(0);
        assertThat(firstThread.getThreadName()).isEqualTo("Test worker");
        assertThat(firstThread.getThreadId()).isEqualTo(1);
        assertThat(firstThread.getBlockedTime()).isEqualTo(-1);
        assertThat(firstThread.getBlockedCount()).isEqualTo(37);
        assertThat(firstThread.getWaitedTime()).isEqualTo(-1);
        assertThat(firstThread.getWaitedCount()).isEqualTo(109);
        assertThat(firstThread.getLockOwnerId()).isEqualTo(-1);
        assertThat(firstThread.daemon()).isFalse();
        assertThat(firstThread.isInNative()).isFalse();
        assertThat(firstThread.isSuspended()).isFalse();
        assertThat(firstThread.getThreadState()).isEqualTo(State.RUNNABLE);
        assertThat(firstThread.getPriority()).isEqualTo(5);
        assertThat(firstThread.getLockInfo()).isNull();
        assertThat(firstThread.getLockName()).isNull();
        assertThat(firstThread.getLockOwnerName()).isNull();

        assertThat(firstThread.getStackTrace()).hasSize(2);
        StackTraceElement firstStackTrace = firstThread.getStackTrace()[0];
        assertThat(firstStackTrace.getModuleName()).isEqualTo("java.management");
        assertThat(firstStackTrace.getModuleVersion()).isEqualTo("17.0.17");
        assertThat(firstStackTrace.getMethodName()).isEqualTo("dumpThreads0");
        assertThat(firstStackTrace.getFileName()).isEqualTo("ThreadImpl.java");
        assertThat(firstStackTrace.getLineNumber()).isEqualTo(-2);
        assertThat(firstStackTrace.getNativeMethod()).isTrue();
        assertThat(firstStackTrace.getClassName()).isEqualTo("sun.management.ThreadImpl");

        StackTraceElement secondStackTrace = firstThread.getStackTrace()[1];
        assertThat(secondStackTrace.getMethodName()).isEqualTo("dumpAllThreads");
        assertThat(secondStackTrace.getLineNumber()).isEqualTo(528);
        assertThat(secondStackTrace.getNativeMethod()).isFalse();

        assertThat(firstThread.getLockedMonitors()).isEmpty();
        assertThat(firstThread.getLockedSynchronizers()).isEmpty();

        ThreadInfo secondThread = threadDumpFeed.getThreads().get(1);
        assertThat(secondThread.getThreadName()).isEqualTo("Finalizer");
        assertThat(secondThread.getThreadId()).isEqualTo(3);
        assertThat(secondThread.getBlockedTime()).isEqualTo(-1);
        assertThat(secondThread.getBlockedCount()).isEqualTo(0);
        assertThat(secondThread.getWaitedTime()).isEqualTo(-1);
        assertThat(secondThread.getWaitedCount()).isEqualTo(1);
        assertThat(secondThread.getLockName()).isEqualTo("java.lang.ref.ReferenceQueue$Lock@60ffdc9f");
        assertThat(secondThread.getLockOwnerId()).isEqualTo(-1);
        assertThat(secondThread.daemon()).isTrue();
        assertThat(secondThread.isInNative()).isFalse();
        assertThat(secondThread.isSuspended()).isFalse();
        assertThat(secondThread.getThreadState()).isEqualTo(State.WAITING);
        assertThat(secondThread.getPriority()).isEqualTo(8);

        assertThat(secondThread.getLockInfo()).isNotNull();
        assertThat(secondThread.getLockInfo().getClassName()).isEqualTo("java.lang.ref.ReferenceQueue$Lock");
        assertThat(secondThread.getLockInfo().getIdentityHashCode()).isEqualTo(1627380895);

        assertThat(secondThread.getStackTrace()).hasSize(2);
        StackTraceElement secondThreadFirstFrame = secondThread.getStackTrace()[0];
        assertThat(secondThreadFirstFrame.getModuleName()).isEqualTo("java.base");
        assertThat(secondThreadFirstFrame.getMethodName()).isEqualTo("wait");
        assertThat(secondThreadFirstFrame.getNativeMethod()).isTrue();
        assertThat(secondThreadFirstFrame.getClassName()).isEqualTo("java.lang.Object");

        StackTraceElement secondThreadSecondFrame = secondThread.getStackTrace()[1];
        assertThat(secondThreadSecondFrame.getFileName()).isEqualTo("Finalizer.java");
        assertThat(secondThreadSecondFrame.getLineNumber()).isEqualTo(172);
        assertThat(secondThreadSecondFrame.getMethodName()).isEqualTo("run");
        assertThat(secondThreadSecondFrame.getNativeMethod()).isFalse();
        assertThat(secondThreadSecondFrame.getClassName()).isEqualTo("java.lang.ref.Finalizer$FinalizerThread");

        assertThat(secondThread.getLockedMonitors()).isEmpty();
        assertThat(secondThread.getLockedSynchronizers()).isEmpty();
    }
}
