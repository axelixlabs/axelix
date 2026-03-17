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

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;

import com.axelixlabs.axelix.common.api.ThreadDumpFeed;

/**
 * Default implementation {@link ThreadDumpBuilder}.
 *
 * @author Sergey Cherkasov
 */
public class DefaultThreadDumpBuilder implements ThreadDumpBuilder {

    private static final ThreadMXBean THREAD_MX_BEAN = ManagementFactory.getThreadMXBean();

    @Override
    public ThreadDumpFeed buildThreadDumpFeed() {
        ThreadInfo[] jmxThreads = THREAD_MX_BEAN.dumpAllThreads(true, true);
        return new ThreadDumpFeed(THREAD_MX_BEAN.isThreadContentionMonitoringEnabled(), jmxThreads);
    }
}
