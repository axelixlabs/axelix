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
package com.axelixlabs.axelix.master.service.convert.response.info.components;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import org.springframework.stereotype.Service;

import com.axelixlabs.axelix.common.api.info.components.ProcessInfo;
import com.axelixlabs.axelix.master.api.external.response.info.components.ProcessProfile;
import com.axelixlabs.axelix.master.service.convert.response.Converter;

/**
 * The {@link Converter} from {@link ProcessInfo} to {@link ProcessProfile}.
 *
 * @author Sergey Cherkasov
 */
@Service
public class ProcessInfoConverter implements Converter<ProcessInfo, ProcessProfile> {

    @Override
    public @NonNull ProcessProfile convertInternal(@NonNull ProcessInfo source) {
        ProcessProfile.Memory memory = convertMemory(source);
        ProcessProfile.VirtualThreads virtualThreads = convertVirtualThreads(source);

        return new ProcessProfile(
                source.pid(), source.parentPid(), source.owner(), memory, virtualThreads, source.cpus());
    }

    private ProcessProfile.@Nullable Memory convertMemory(ProcessInfo source) {
        if (source.memory() != null) {
            ProcessProfile.Memory.Heap heap = convertHeap(source);
            ProcessProfile.Memory.NonHeap nonHeap = convertNonHeap(source);
            Set<ProcessProfile.Memory.GarbageCollectors> garbageCollectors = convertGarbageCollectors(source);

            return new ProcessProfile.Memory(heap, nonHeap, garbageCollectors);
        }

        return null;
    }

    private ProcessProfile.Memory.@Nullable Heap convertHeap(ProcessInfo source) {
        ProcessInfo.Memory memory = source.memory();
        if (memory != null && memory.heap() != null) {
            ProcessInfo.Memory.Heap heap = memory.heap();
            return new ProcessProfile.Memory.Heap(heap.max(), heap.used(), heap.committed(), heap.init());
        }

        return null;
    }

    private ProcessProfile.Memory.@Nullable NonHeap convertNonHeap(ProcessInfo source) {
        ProcessInfo.Memory memory = source.memory();
        if (memory != null && memory.nonHeap() != null) {
            ProcessInfo.Memory.NonHeap nonHeap = memory.nonHeap();
            return new ProcessProfile.Memory.NonHeap(
                    nonHeap.max(), nonHeap.used(), nonHeap.committed(), nonHeap.init());
        }

        return null;
    }

    private Set<ProcessProfile.Memory.GarbageCollectors> convertGarbageCollectors(ProcessInfo source) {
        ProcessInfo.Memory memory = source.memory();
        if (memory != null) {
            Set<ProcessInfo.Memory.GarbageCollectors> garbageCollectors = memory.garbageCollectors();
            if (garbageCollectors == null || garbageCollectors.isEmpty()) {
                return Collections.emptySet();
            }

            return garbageCollectors.stream()
                    .map(gc -> new ProcessProfile.Memory.GarbageCollectors(gc.name(), gc.collectionCount()))
                    .collect(Collectors.toSet());
        }

        return Collections.emptySet();
    }

    private ProcessProfile.@Nullable VirtualThreads convertVirtualThreads(ProcessInfo source) {
        if (source.virtualThreads() != null) {
            ProcessInfo.VirtualThreads vt = source.virtualThreads();
            return new ProcessProfile.VirtualThreads(vt.mounted(), vt.queued(), vt.parallelism(), vt.poolSize());
        }

        return null;
    }
}
