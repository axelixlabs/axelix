package com.nucleonforge.axile.master.service.convert;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import org.springframework.stereotype.Service;

import com.nucleonforge.axile.common.api.ThreadDumpFeed;
import com.nucleonforge.axile.master.api.response.ThreadDumpFeedResponse;
import com.nucleonforge.axile.master.api.response.ThreadDumpFeedResponse.LockInfo;
import com.nucleonforge.axile.master.api.response.ThreadDumpFeedResponse.MonitorInfo;
import com.nucleonforge.axile.master.api.response.ThreadDumpFeedResponse.StackTraceElement;
import com.nucleonforge.axile.master.api.response.ThreadDumpFeedResponse.State;
import com.nucleonforge.axile.master.api.response.ThreadDumpFeedResponse.ThreadInfo;

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

        return new ThreadDumpFeedResponse(result);
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
