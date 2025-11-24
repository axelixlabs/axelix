package com.nucleonforge.axile.master.service.export.collect;

import org.springframework.stereotype.Component;

import com.nucleonforge.axile.master.api.ThreadDumpApi;
import com.nucleonforge.axile.master.service.export.StateComponent;
import com.nucleonforge.axile.master.service.export.settings.ThreadDumpStateComponentSettings;

/**
 * Collects Thread Dump information for application state export.
 *
 * @see ThreadDumpApi
 * @since 20.11.2025
 * @author Nikita Kirillov
 */
@Component
public class ThreadDumpContributorJsonInstance
        extends AbstractJsonInstanceStateCollector<ThreadDumpStateComponentSettings> {

    private final ThreadDumpApi threadDumpApi;

    public ThreadDumpContributorJsonInstance(ThreadDumpApi threadDumpApi) {
        this.threadDumpApi = threadDumpApi;
    }

    @Override
    public StateComponent responsibleFor() {
        return StateComponent.THREAD_DUMP;
    }

    @Override
    protected Object collectInternal(String instanceId, ThreadDumpStateComponentSettings settings) {
        return threadDumpApi.getThreadDump(instanceId);
    }
}
