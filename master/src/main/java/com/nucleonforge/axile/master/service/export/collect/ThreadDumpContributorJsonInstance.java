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
