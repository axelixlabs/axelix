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
package com.axelixlabs.axelix.sbs.spring.threaddump;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;

import org.springframework.boot.actuate.endpoint.web.annotation.RestControllerEndpoint;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import com.axelixlabs.axelix.common.api.ThreadDumpFeed;

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
