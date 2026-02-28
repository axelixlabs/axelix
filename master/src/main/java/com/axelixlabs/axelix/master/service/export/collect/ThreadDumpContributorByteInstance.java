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
package com.axelixlabs.axelix.master.service.export.collect;

import org.springframework.stereotype.Component;

import com.axelixlabs.axelix.master.api.external.endpoint.ThreadDumpApi;
import com.axelixlabs.axelix.master.service.export.StateComponent;
import com.axelixlabs.axelix.master.service.export.settings.ThreadDumpStateComponentSettings;

/**
 * Collects Thread Dump information for application state export.
 *
 * @see ThreadDumpApi
 * @since 20.11.2025
 * @author Nikita Kirillov
 * @author Sergey Cherkasov
 */
@Component
public class ThreadDumpContributorByteInstance
        extends AbstractByteInstanceStateCollector<ThreadDumpStateComponentSettings> {

    private final ThreadDumpApi threadDumpApi;

    public ThreadDumpContributorByteInstance(ThreadDumpApi threadDumpApi) {
        this.threadDumpApi = threadDumpApi;
    }

    @Override
    public StateComponent responsibleFor() {
        return StateComponent.THREAD_DUMP;
    }

    @Override
    protected byte[] collectByte(String instanceId, ThreadDumpStateComponentSettings settings) {
        return threadDumpApi.getThreadDump(instanceId);
    }
}
