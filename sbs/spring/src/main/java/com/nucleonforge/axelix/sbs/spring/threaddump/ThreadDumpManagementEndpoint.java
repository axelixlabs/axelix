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
package com.nucleonforge.axelix.sbs.spring.threaddump;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;

import org.springframework.boot.actuate.endpoint.web.annotation.RestControllerEndpoint;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import com.nucleonforge.axelix.common.api.ThreadDumpFeed;

/**
 * Custom Spring Boot Actuator endpoint to enable or disable thread contention monitoring.
 *
 * @author Sergey Cherkasov
 */
@RestControllerEndpoint(id = "axelix-thread-dump")
public class ThreadDumpManagementEndpoint {

    private static final ThreadMXBean THREAD_MX_BEAN = ManagementFactory.getThreadMXBean();

    private final ThreadDumpContentionMonitoringManagement management;

    public ThreadDumpManagementEndpoint(ThreadDumpContentionMonitoringManagement management) {
        this.management = management;
    }

    @GetMapping
    public ThreadDumpFeed getThreadDump() {
        ThreadInfo[] jmxThreads = THREAD_MX_BEAN.dumpAllThreads(true, true);
        return new ThreadDumpFeed(THREAD_MX_BEAN.isThreadContentionMonitoringEnabled(), jmxThreads);
    }

    @PostMapping("/enable")
    private void enableContentionMonitoring() {
        management.enable();
    }

    @PostMapping("/disable")
    private void disableContentionMonitoring() {
        management.disable();
    }
}
