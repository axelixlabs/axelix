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
package com.axelixlabs.axelix.common.api.info.components;

import java.util.Objects;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.jspecify.annotations.Nullable;

/**
 * DTO that encapsulates the process information of the given artifact.
 *
 * @author Sergey Cherkasov
 */
public final class ProcessInfo {

    private final Long pid;
    private final Long parentPid;
    private final String owner;

    @Nullable
    private final Memory memory;

    @Nullable
    private final VirtualThreads virtualThreads;

    private final Integer cpus;

    public ProcessInfo(
            @JsonProperty("pid") Long pid,
            @JsonProperty("parentPid") Long parentPid,
            @JsonProperty("owner") String owner,
            @JsonProperty("memory") @Nullable Memory memory,
            @JsonProperty("virtualThreads") @Nullable VirtualThreads virtualThreads,
            @JsonProperty("cpus") Integer cpus) {
        this.pid = pid;
        this.parentPid = parentPid;
        this.owner = owner;
        this.memory = memory;
        this.virtualThreads = virtualThreads;
        this.cpus = cpus;
    }

    public Long pid() {
        return pid;
    }

    public Long parentPid() {
        return parentPid;
    }

    public String owner() {
        return owner;
    }

    @Nullable
    public Memory memory() {
        return memory;
    }

    @Nullable
    public VirtualThreads virtualThreads() {
        return virtualThreads;
    }

    public Integer cpus() {
        return cpus;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ProcessInfo that = (ProcessInfo) o;
        return Objects.equals(pid, that.pid)
                && Objects.equals(parentPid, that.parentPid)
                && Objects.equals(owner, that.owner)
                && Objects.equals(memory, that.memory)
                && Objects.equals(virtualThreads, that.virtualThreads)
                && Objects.equals(cpus, that.cpus);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pid, parentPid, owner, memory, virtualThreads, cpus);
    }

    @Override
    public String toString() {
        return "ProcessInfo{"
                + "pid="
                + pid
                + ", parentPid="
                + parentPid
                + ", owner='"
                + owner
                + '\''
                + ", memory="
                + memory
                + ", virtualThreads="
                + virtualThreads
                + ", cpus="
                + cpus
                + '}';
    }

    public static final class Memory {

        @Nullable
        private final Heap heap;

        @Nullable
        private final NonHeap nonHeap;

        @Nullable
        private final Set<GarbageCollectors> garbageCollectors;

        public Memory(
                @JsonProperty("heap") @Nullable Heap heap,
                @JsonProperty("nonHeap") @Nullable NonHeap nonHeap,
                @JsonProperty("garbageCollectors") @Nullable Set<GarbageCollectors> garbageCollectors) {
            this.heap = heap;
            this.nonHeap = nonHeap;
            this.garbageCollectors = garbageCollectors;
        }

        @Nullable
        public Heap heap() {
            return heap;
        }

        @Nullable
        public NonHeap nonHeap() {
            return nonHeap;
        }

        @Nullable
        public Set<GarbageCollectors> garbageCollectors() {
            return garbageCollectors;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            Memory memory = (Memory) o;
            return Objects.equals(heap, memory.heap)
                    && Objects.equals(nonHeap, memory.nonHeap)
                    && Objects.equals(garbageCollectors, memory.garbageCollectors);
        }

        @Override
        public int hashCode() {
            return Objects.hash(heap, nonHeap, garbageCollectors);
        }

        @Override
        public String toString() {
            return "Memory{"
                    + "heap="
                    + heap
                    + ", nonHeap="
                    + nonHeap
                    + ", garbageCollectors="
                    + garbageCollectors
                    + '}';
        }

        public static final class Heap {

            private final Long max;
            private final Long used;
            private final Long committed;
            private final Long init;

            public Heap(
                    @JsonProperty("max") Long max,
                    @JsonProperty("used") Long used,
                    @JsonProperty("committed") Long committed,
                    @JsonProperty("init") Long init) {
                this.max = max;
                this.used = used;
                this.committed = committed;
                this.init = init;
            }

            public Long max() {
                return max;
            }

            public Long used() {
                return used;
            }

            public Long committed() {
                return committed;
            }

            public Long init() {
                return init;
            }

            @Override
            public boolean equals(Object o) {
                if (this == o) {
                    return true;
                }
                if (o == null || getClass() != o.getClass()) {
                    return false;
                }
                Heap heap = (Heap) o;
                return Objects.equals(max, heap.max)
                        && Objects.equals(used, heap.used)
                        && Objects.equals(committed, heap.committed)
                        && Objects.equals(init, heap.init);
            }

            @Override
            public int hashCode() {
                return Objects.hash(max, used, committed, init);
            }

            @Override
            public String toString() {
                return "Heap{" + "max=" + max + ", used=" + used + ", committed=" + committed + ", init=" + init + '}';
            }
        }

        public static final class NonHeap {

            private final Long max;
            private final Long used;
            private final Long committed;
            private final Long init;

            public NonHeap(
                    @JsonProperty("max") Long max,
                    @JsonProperty("used") Long used,
                    @JsonProperty("committed") Long committed,
                    @JsonProperty("init") Long init) {
                this.max = max;
                this.used = used;
                this.committed = committed;
                this.init = init;
            }

            public Long max() {
                return max;
            }

            public Long used() {
                return used;
            }

            public Long committed() {
                return committed;
            }

            public Long init() {
                return init;
            }

            @Override
            public boolean equals(Object o) {
                if (this == o) {
                    return true;
                }
                if (o == null || getClass() != o.getClass()) {
                    return false;
                }
                NonHeap nonHeap = (NonHeap) o;
                return Objects.equals(max, nonHeap.max)
                        && Objects.equals(used, nonHeap.used)
                        && Objects.equals(committed, nonHeap.committed)
                        && Objects.equals(init, nonHeap.init);
            }

            @Override
            public int hashCode() {
                return Objects.hash(max, used, committed, init);
            }

            @Override
            public String toString() {
                return "NonHeap{"
                        + "max="
                        + max
                        + ", used="
                        + used
                        + ", committed="
                        + committed
                        + ", init="
                        + init
                        + '}';
            }
        }

        public static final class GarbageCollectors {

            private final String name;
            private final Integer collectionCount;

            public GarbageCollectors(
                    @JsonProperty("name") String name, @JsonProperty("collectionCount") Integer collectionCount) {
                this.name = name;
                this.collectionCount = collectionCount;
            }

            public String name() {
                return name;
            }

            public Integer collectionCount() {
                return collectionCount;
            }

            @Override
            public boolean equals(Object o) {
                if (this == o) {
                    return true;
                }
                if (o == null || getClass() != o.getClass()) {
                    return false;
                }
                GarbageCollectors that = (GarbageCollectors) o;
                return Objects.equals(name, that.name) && Objects.equals(collectionCount, that.collectionCount);
            }

            @Override
            public int hashCode() {
                return Objects.hash(name, collectionCount);
            }

            @Override
            public String toString() {
                return "GarbageCollectors{" + "name='" + name + '\'' + ", collectionCount=" + collectionCount + '}';
            }
        }
    }

    public static final class VirtualThreads {

        private final Long mounted;
        private final Long queued;
        private final Long parallelism;
        private final Long poolSize;

        public VirtualThreads(
                @JsonProperty("mounted") Long mounted,
                @JsonProperty("queued") Long queued,
                @JsonProperty("parallelism") Long parallelism,
                @JsonProperty("poolSize") Long poolSize) {
            this.mounted = mounted;
            this.queued = queued;
            this.parallelism = parallelism;
            this.poolSize = poolSize;
        }

        public Long mounted() {
            return mounted;
        }

        public Long queued() {
            return queued;
        }

        public Long parallelism() {
            return parallelism;
        }

        public Long poolSize() {
            return poolSize;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            VirtualThreads that = (VirtualThreads) o;
            return Objects.equals(mounted, that.mounted)
                    && Objects.equals(queued, that.queued)
                    && Objects.equals(parallelism, that.parallelism)
                    && Objects.equals(poolSize, that.poolSize);
        }

        @Override
        public int hashCode() {
            return Objects.hash(mounted, queued, parallelism, poolSize);
        }

        @Override
        public String toString() {
            return "VirtualThreads{"
                    + "mounted="
                    + mounted
                    + ", queued="
                    + queued
                    + ", parallelism="
                    + parallelism
                    + ", poolSize="
                    + poolSize
                    + '}';
        }
    }
}
