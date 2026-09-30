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

import java.lang.management.MonitorInfo;

import org.junit.jupiter.api.Test;

import com.axelixlabs.axelix.sbs.spring.core.contract.threaddump.StackTraceElement;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link ThreadDumpFeedAssembler}.
 *
 * @author Nikita Kirillov
 */
class ThreadDumpFeedAssemblerTest {

    @Test
    void shouldTolerateALockedMonitorWithNoStackFrame() {
        // given. a monitor dump with no available stack frame, e.g. taken with a limited stack depth.
        MonitorInfo jmxMonitor = new MonitorInfo("java.lang.Object", 42, -1, null);

        // when.
        var monitorInfo = ThreadDumpFeedAssembler.toMonitorInfo(jmxMonitor);

        // then.
        assertThat(monitorInfo.getClassName()).isEqualTo("java.lang.Object");
        assertThat(monitorInfo.getIdentityHashCode()).isEqualTo(42);
        assertThat(monitorInfo.getLockedStackDepth()).isEqualTo(-1);
        assertThat(monitorInfo.getLockedStackFrame()).isNull();
    }

    @Test
    void shouldConvertALockedMonitorWithAStackFrame() {
        // given.
        java.lang.StackTraceElement frame =
                new java.lang.StackTraceElement("com.example.Owner", "lock", "Owner.java", 42);
        MonitorInfo jmxMonitor = new MonitorInfo("java.lang.Object", 42, 0, frame);

        // when.
        var monitorInfo = ThreadDumpFeedAssembler.toMonitorInfo(jmxMonitor);

        // then.
        StackTraceElement lockedStackFrame = monitorInfo.getLockedStackFrame();
        assertThat(lockedStackFrame).isNotNull();
        assertThat(lockedStackFrame.getClassName()).isEqualTo("com.example.Owner");
        assertThat(lockedStackFrame.getMethodName()).isEqualTo("lock");
        assertThat(lockedStackFrame.getLineNumber()).isEqualTo(42);
    }
}
