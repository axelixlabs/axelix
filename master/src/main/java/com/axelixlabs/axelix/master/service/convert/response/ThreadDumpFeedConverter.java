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
package com.axelixlabs.axelix.master.service.convert.response;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import org.springframework.stereotype.Service;

import com.axelixlabs.axelix.common.api.ThreadDumpFeed;
import com.axelixlabs.axelix.master.api.response.ThreadDumpFeedResponse;
import com.axelixlabs.axelix.master.api.response.ThreadDumpFeedResponse.LockInfo;
import com.axelixlabs.axelix.master.api.response.ThreadDumpFeedResponse.MonitorInfo;
import com.axelixlabs.axelix.master.api.response.ThreadDumpFeedResponse.StackTraceElement;
import com.axelixlabs.axelix.master.api.response.ThreadDumpFeedResponse.State;
import com.axelixlabs.axelix.master.api.response.ThreadDumpFeedResponse.ThreadInfo;

/**
 * The {@link Converter} from {@link ThreadDumpFeed} to {@link ThreadDumpFeedResponse}.
 *
 * @since 18.11.2025
 * @author Nikita Kirillov
 */
@Service
public class ThreadDumpFeedConverter implements Converter<ThreadDumpFeed, ThreadDumpFeedResponse> {

    @Override
    public @NonNull ThreadDumpFeedResponse convertInternal(@NonNull ThreadDumpFeed source) {
        List<ThreadInfo> result = new ArrayList<>();

        source.threads()
                .forEach(currentThread -> result.add(new ThreadInfo(
                        currentThread.threadName(),
                        currentThread.threadId(),
                        currentThread.blockedTime(),
                        currentThread.blockedCount(),
                        currentThread.waitedTime(),
                        currentThread.waitedCount(),
                        convertLockInfo(currentThread.lockInfo()),
                        currentThread.lockName(),
                        currentThread.lockOwnerId(),
                        currentThread.lockOwnerName(),
                        currentThread.daemon(),
                        currentThread.inNative(),
                        currentThread.suspended(),
                        convertState(currentThread.threadState()),
                        currentThread.priority(),
                        Arrays.stream(currentThread.stackTrace())
                                .map(this::convertStackTraceElement)
                                .toArray(StackTraceElement[]::new),
                        Arrays.stream(currentThread.lockedMonitors())
                                .map(this::convertMonitorInfo)
                                .toArray(MonitorInfo[]::new),
                        Arrays.stream(currentThread.lockedSynchronizers())
                                .map(this::convertLockInfo)
                                .toArray(LockInfo[]::new))));

        return new ThreadDumpFeedResponse(source.threadContentionMonitoringEnabled(), result);
    }

    private @Nullable LockInfo convertLockInfo(ThreadDumpFeed.@Nullable LockInfo source) {
        if (source == null) {
            return null;
        }
        return new LockInfo(source.className(), source.identityHashCode());
    }

    private State convertState(ThreadDumpFeed.State source) {
        return switch (source) {
            case NEW -> State.NEW;
            case RUNNABLE -> State.RUNNABLE;
            case BLOCKED -> State.BLOCKED;
            case WAITING -> State.WAITING;
            case TIMED_WAITING -> State.TIMED_WAITING;
            case TERMINATED -> State.TERMINATED;
        };
    }

    private StackTraceElement convertStackTraceElement(ThreadDumpFeed.StackTraceElement source) {
        return new StackTraceElement(
                source.classLoaderName(),
                source.className(),
                source.fileName(),
                source.lineNumber(),
                source.methodName(),
                source.moduleName(),
                source.moduleVersion(),
                source.nativeMethod());
    }

    private MonitorInfo convertMonitorInfo(ThreadDumpFeed.MonitorInfo source) {
        return new MonitorInfo(
                source.className(),
                source.identityHashCode(),
                source.lockedStackDepth(),
                convertStackTraceElement(source.lockedStackFrame()));
    }
}
