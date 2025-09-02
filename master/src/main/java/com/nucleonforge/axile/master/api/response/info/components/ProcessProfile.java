package com.nucleonforge.axile.master.api.response.info.components;

import java.util.Collections;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.jspecify.annotations.Nullable;

/**
 * The profile of a given process.
 *
 * @param pid             The ID of the process.
 * @param parentPid       The ID of the parent process.
 * @param owner           The owner of the process.
 * @param memory          The Memory information of the process, if available (present since version 3.4.9).
 * @param virtualThreads  The virtual thread information of the process (if VirtualThreadSchedulerMXBean is available, present since version 3.5.5).
 * @param cpus            The number of CPUs available to the process.
 *
 * @author Sergey Cherkasov
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ProcessProfile(
        Long pid,
        Long parentPid,
        String owner,
        @Nullable Memory memory,
        @Nullable VirtualThreads virtualThreads,
        Integer cpus) {

    /**
     * The profile of a given memory.
     *
     * @param heap               The heap of the memory.
     * @param nonHeap            The non-heap of the memory.
     * @param garbageCollectors  The details of the garbage collectors.
     *
     * @author Sergey Cherkasov
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record Memory(@Nullable Heap heap, @Nullable NonHeap nonHeap, Set<GarbageCollectors> garbageCollectors) {

        public Memory {
            if (garbageCollectors == null) {
                garbageCollectors = Collections.emptySet();
            }
        }

        /**
         * The profile of a given heap.
         *
         * @param max         The maximum number of bytes that can be used by the JVM (or -1).
         * @param used        The number of bytes currently being used.
         * @param committed   The number of bytes committed for JVM use.
         * @param init        The number of bytes initially requested by the JVM.
         *
         * @author Sergey Cherkasov
         */
        @JsonInclude(JsonInclude.Include.NON_NULL)
        public record Heap(Long max, Long used, Long committed, Long init) {}

        /**
         * The profile of a given non-heap.
         *
         * @param max         The maximum number of bytes that can be used by the JVM (or -1).
         * @param used        The number of bytes currently being used.
         * @param committed   The number of bytes committed for JVM use.
         * @param init        The number of bytes initially requested by the JVM.
         *
         * @author Sergey Cherkasov
         */
        @JsonInclude(JsonInclude.Include.NON_NULL)
        public record NonHeap(Long max, Long used, Long committed, Long init) {}

        /**
         * The profile of a given garbage collector.
         *
         * @param name             The name of the garbage collector.
         * @param collectionCount  The total number of collections that have occurred.
         *
         * @author Sergey Cherkasov
         */
        @JsonInclude(JsonInclude.Include.NON_NULL)
        public record GarbageCollectors(String name, Integer collectionCount) {}
    }

    /**
     * The profile of a given virtual thread.
     *
     * @param mounted       The estimate of the number of virtual threads currently mounted by the scheduler.
     * @param queued        The estimate of the number of virtual threads queued to the scheduler to start or continue execution.
     * @param parallelism   The scheduler’s target parallelism.
     * @param poolSize      The current number of platform threads that the scheduler has started but have not terminated.
     *
     * @author Sergey Cherkasov
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record VirtualThreads(Long mounted, Long queued, Long parallelism, Long poolSize) {}
}
