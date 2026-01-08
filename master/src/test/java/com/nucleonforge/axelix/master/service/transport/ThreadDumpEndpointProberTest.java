/*
 * Copyright 2025-present, Nucleon Forge Software.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.nucleonforge.axelix.master.service.transport;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.nucleonforge.axelix.common.api.ThreadDumpFeed;
import com.nucleonforge.axelix.common.api.ThreadDumpFeed.LockInfo;
import com.nucleonforge.axelix.common.api.ThreadDumpFeed.MonitorInfo;
import com.nucleonforge.axelix.common.api.ThreadDumpFeed.StackTraceElement;
import com.nucleonforge.axelix.common.api.ThreadDumpFeed.State;
import com.nucleonforge.axelix.common.api.ThreadDumpFeed.ThreadInfo;
import com.nucleonforge.axelix.common.domain.http.NoHttpPayload;
import com.nucleonforge.axelix.master.ApplicationEntrypoint;
import com.nucleonforge.axelix.master.exception.InstanceNotFoundException;
import com.nucleonforge.axelix.master.model.instance.InstanceId;
import com.nucleonforge.axelix.master.service.state.InstanceRegistry;
import com.nucleonforge.axelix.master.service.transport.threaddump.ThreadDumpDisableContentionMonitoringProber;
import com.nucleonforge.axelix.master.service.transport.threaddump.ThreadDumpEnableContentionMonitoringProber;
import com.nucleonforge.axelix.master.service.transport.threaddump.ThreadDumpEndpointProber;
import com.nucleonforge.axelix.master.utils.TestObjectFactory;

import static com.nucleonforge.axelix.master.utils.ContentType.ACTUATOR_RESPONSE_CONTENT_TYPE;
import static com.nucleonforge.axelix.master.utils.TestObjectFactory.createInstance;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Integration tests for {@link ThreadDumpEndpointProber}.
 *
 * @since 19.11.2025
 * @author Nikita Kirillov
 * @author Sergey Cherkasov
 */
@SpringBootTest(classes = ApplicationEntrypoint.class)
class ThreadDumpEndpointProberTest {

    private static final String activeInstanceId = UUID.randomUUID().toString();

    private static MockWebServer mockWebServer;

    @Autowired
    private InstanceRegistry registry;

    @Autowired
    private ThreadDumpEndpointProber threadDumpEndpointProber;

    @Autowired
    private ThreadDumpEnableContentionMonitoringProber enableContentionMonitoringProber;

    @Autowired
    private ThreadDumpDisableContentionMonitoringProber disableContentionMonitoringProber;

    @BeforeAll
    static void startServer() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
    }

    @AfterAll
    static void shutdownServer() throws IOException {
        mockWebServer.shutdown();
    }

    @BeforeEach
    void prepare() {
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
          "lockedMonitors": [
            {
              "className": "java.io.InputStreamReader",
              "identityHashCode": 2012856739,
              "lockedStackDepth": 12,
              "lockedStackFrame": {
              "moduleName": "java.base",
              "moduleVersion": "17.0.16",
              "methodName": "readLine",
              "fileName": "BufferedReader.java",
              "lineNumber": 329,
              "nativeMethod": false,
              "className": "java.io.BufferedReader",
              "classLoaderName": "app"
              }
            } ],
            "lockedSynchronizers": [
              {
                "className": "java.util.concurrent.locks.ReentrantLock$NonfairSync",
                "identityHashCode": 2019322618
              } ]
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
            "className" : "java.lang.ref.Finalizer$FinalizerThread",
            "classLoaderName": "app"
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

        mockWebServer.setDispatcher(new Dispatcher() {
            @Override
            public @NotNull MockResponse dispatch(@NotNull RecordedRequest request) {
                String path = request.getPath();
                assert path != null;

                if (path.equals("/" + activeInstanceId + "/actuator/axelix-thread-dump")) {
                    return new MockResponse()
                            .setBody(jsonResponse)
                            .addHeader("Content-Type", ACTUATOR_RESPONSE_CONTENT_TYPE);
                } else if (path.equals("/" + activeInstanceId + "/actuator/axelix-thread-dump/enable")) {
                    return new MockResponse();
                } else if (path.equals("/" + activeInstanceId + "/actuator/axelix-thread-dump/disable")) {
                    return new MockResponse();
                } else {
                    return new MockResponse().setResponseCode(404);
                }
            }
        });
        registry.register(
                TestObjectFactory.createInstance(activeInstanceId, mockWebServer.url(activeInstanceId) + "/actuator"));
    }

    @AfterEach
    void cleanup() {
        registry.deRegister(InstanceId.of(activeInstanceId));
    }

    @Test
    void shouldReturnThreadDumpFeed() {

        ThreadDumpFeed feed = threadDumpEndpointProber.invoke(InstanceId.of(activeInstanceId), NoHttpPayload.INSTANCE);

        assertThat(feed).isNotNull();

        List<ThreadInfo> threads = feed.threads();
        assertThat(threads).hasSize(2);

        ThreadInfo thread1 = threads.get(0);
        assertThat(thread1.threadName()).isEqualTo("Test worker");
        assertThat(thread1.threadId()).isEqualTo(1);
        assertThat(thread1.blockedTime()).isEqualTo(-1);
        assertThat(thread1.blockedCount()).isEqualTo(37);
        assertThat(thread1.waitedTime()).isEqualTo(-1);
        assertThat(thread1.waitedCount()).isEqualTo(109);
        assertThat(thread1.lockOwnerId()).isEqualTo(-1);
        assertThat(thread1.daemon()).isFalse();
        assertThat(thread1.inNative()).isFalse();
        assertThat(thread1.suspended()).isFalse();
        assertThat(thread1.threadState()).isEqualTo(State.RUNNABLE);
        assertThat(thread1.priority()).isEqualTo(5);

        StackTraceElement[] testWorkerStackTrace = thread1.stackTrace();
        assertThat(testWorkerStackTrace).hasSize(2);

        StackTraceElement firstFrame = testWorkerStackTrace[0];
        assertThat(firstFrame.moduleName()).isEqualTo("java.management");
        assertThat(firstFrame.moduleVersion()).isEqualTo("17.0.17");
        assertThat(firstFrame.methodName()).isEqualTo("dumpThreads0");
        assertThat(firstFrame.fileName()).isEqualTo("ThreadImpl.java");
        assertThat(firstFrame.lineNumber()).isEqualTo(-2);
        assertThat(firstFrame.nativeMethod()).isTrue();
        assertThat(firstFrame.className()).isEqualTo("sun.management.ThreadImpl");

        StackTraceElement secondFrame = testWorkerStackTrace[1];
        assertThat(secondFrame.methodName()).isEqualTo("dumpAllThreads");
        assertThat(secondFrame.lineNumber()).isEqualTo(528);
        assertThat(secondFrame.nativeMethod()).isFalse();

        MonitorInfo monitorInfo = thread1.lockedMonitors()[0];
        assertThat(monitorInfo.className()).isEqualTo("java.io.InputStreamReader");
        assertThat(monitorInfo.identityHashCode()).isEqualTo(2012856739);
        assertThat(monitorInfo.lockedStackDepth()).isEqualTo(12);

        StackTraceElement innerStackTrace = monitorInfo.lockedStackFrame();
        assertThat(innerStackTrace.moduleName()).isEqualTo("java.base");
        assertThat(innerStackTrace.moduleVersion()).isEqualTo("17.0.16");
        assertThat(innerStackTrace.methodName()).isEqualTo("readLine");
        assertThat(innerStackTrace.fileName()).isEqualTo("BufferedReader.java");
        assertThat(innerStackTrace.lineNumber()).isEqualTo(329);
        assertThat(innerStackTrace.nativeMethod()).isEqualTo(false);
        assertThat(innerStackTrace.className()).isEqualTo("java.io.BufferedReader");
        assertThat(innerStackTrace.classLoaderName()).isEqualTo("app");

        LockInfo lockedSynchrinizer = thread1.lockedSynchronizers()[0];
        assertThat(lockedSynchrinizer.className()).isEqualTo("java.util.concurrent.locks.ReentrantLock$NonfairSync");
        assertThat(lockedSynchrinizer.identityHashCode()).isEqualTo(2019322618);

        ThreadInfo thread2 = threads.get(1);
        assertThat(thread2.threadName()).isEqualTo("Finalizer");
        assertThat(thread2.threadId()).isEqualTo(3);
        assertThat(thread2.blockedTime()).isEqualTo(-1);
        assertThat(thread2.blockedCount()).isEqualTo(0);
        assertThat(thread2.waitedTime()).isEqualTo(-1);
        assertThat(thread2.waitedCount()).isEqualTo(1);
        assertThat(thread2.lockName()).isEqualTo("java.lang.ref.ReferenceQueue$Lock@60ffdc9f");
        assertThat(thread2.lockOwnerId()).isEqualTo(-1);
        assertThat(thread2.daemon()).isTrue();
        assertThat(thread2.inNative()).isFalse();
        assertThat(thread2.suspended()).isFalse();
        assertThat(thread2.threadState()).isEqualTo(State.WAITING);
        assertThat(thread2.priority()).isEqualTo(8);

        StackTraceElement[] finalizerStackTrace = thread2.stackTrace();
        assertThat(finalizerStackTrace).hasSize(2);

        StackTraceElement finalizerFirstFrame = finalizerStackTrace[0];
        assertThat(finalizerFirstFrame.moduleName()).isEqualTo("java.base");
        assertThat(finalizerFirstFrame.methodName()).isEqualTo("wait");
        assertThat(finalizerFirstFrame.nativeMethod()).isTrue();
        assertThat(finalizerFirstFrame.className()).isEqualTo("java.lang.Object");

        StackTraceElement finalizerSecondFrame = finalizerStackTrace[1];
        assertThat(finalizerSecondFrame.fileName()).isEqualTo("Finalizer.java");
        assertThat(finalizerSecondFrame.lineNumber()).isEqualTo(172);
        assertThat(finalizerSecondFrame.methodName()).isEqualTo("run");
        assertThat(finalizerSecondFrame.nativeMethod()).isFalse();
        assertThat(finalizerSecondFrame.className()).isEqualTo("java.lang.ref.Finalizer$FinalizerThread");

        assertThat(thread2.lockedMonitors()).isEmpty();
        assertThat(thread2.lockedSynchronizers()).isEmpty();

        LockInfo lockInfo = thread2.lockInfo();
        assertThat(lockInfo).isNotNull();
        assertThat(lockInfo.className()).isEqualTo("java.lang.ref.ReferenceQueue$Lock");
        assertThat(lockInfo.identityHashCode()).isEqualTo(1627380895);
    }

    @Test
    void shouldThrowExceptionWhenInstanceUrlIsUnreachable() {
        String instanceId = UUID.randomUUID().toString();

        registry.register(createInstance(instanceId));

        assertThatThrownBy(() -> threadDumpEndpointProber.invoke(InstanceId.of(instanceId), NoHttpPayload.INSTANCE))
                .isInstanceOf(EndpointInvocationException.class);
    }

    @Test
    void shouldThrowExceptionForUnregisteredInstance() {
        String instanceId = UUID.randomUUID().toString();

        assertThatThrownBy(() -> threadDumpEndpointProber.invoke(InstanceId.of(instanceId), NoHttpPayload.INSTANCE))
                .isInstanceOf(InstanceNotFoundException.class);
    }

    @Test
    void shouldEnableContentionMonitoring() throws InterruptedException {
        // when.
        enableContentionMonitoringProber.invoke(InstanceId.of(activeInstanceId), NoHttpPayload.INSTANCE);

        // then.
        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        assertThat(recordedRequest.getPath()).isEqualTo("/" + activeInstanceId + "/actuator/axelix-thread-dump/enable");
    }

    @Test
    void shouldDisableContentionMonitoring() throws InterruptedException {
        // when.
        disableContentionMonitoringProber.invoke(InstanceId.of(activeInstanceId), NoHttpPayload.INSTANCE);

        // then.
        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        assertThat(recordedRequest.getPath())
                .isEqualTo("/" + activeInstanceId + "/actuator/axelix-thread-dump/disable");
    }
}
