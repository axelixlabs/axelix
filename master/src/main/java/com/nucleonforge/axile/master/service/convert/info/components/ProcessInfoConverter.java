package com.nucleonforge.axile.master.service.convert.info.components;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import org.springframework.stereotype.Service;

import com.nucleonforge.axile.common.api.info.components.ProcessInfo;
import com.nucleonforge.axile.master.api.response.info.components.ProcessProfile;
import com.nucleonforge.axile.master.service.convert.Converter;

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
