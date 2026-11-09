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
package com.axelixlabs.axelix.sbs.spring.core;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Threads;
import org.openjdk.jmh.annotations.Warmup;

/**
 * JMH benchmarks for {@link SlidingWindow}.
 *
 * @author Nikita Kirillov
 */
@BenchmarkMode({Mode.AverageTime, Mode.SampleTime})
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 5, time = 1)
@Fork(
        value = 2,
        jvmArgsAppend = {"-Xms512m", "-Xmx512m"})
public class SlidingWindowBenchmark {

    @State(Scope.Benchmark)
    public static class WindowState {

        @Param({"10", "200", "1000"})
        public int capacity;

        @Param({"STRICT", "BEST_EFFORT"})
        public SlidingWindow.WriteMode writeMode;

        private SlidingWindow<Object> window;
        private final Object element = new Object();

        @Setup
        public void setUp() {
            window = new SlidingWindow<>(capacity, writeMode);
            for (int i = 0; i < capacity; i++) {
                window.put(element);
            }
        }
    }

    @State(Scope.Benchmark)
    public static class SequenceState {
        private final AtomicLong sequence = new AtomicLong();
    }

    @State(Scope.Thread)
    public static class AllocationState {
        private final Object element = new Object();
        private long sequence;
    }

    /** Matches the internal entry layout for allocation measurements. */
    @SuppressWarnings({"FieldCanBeLocal", "unused"})
    public static final class ProbeEntry {
        private final long sequence;
        private final Object value;

        private ProbeEntry(long sequence, Object value) {
            this.sequence = sequence;
            this.value = value;
        }
    }

    @Benchmark
    @Threads(1)
    public void put(WindowState state) {
        state.window.put(state.element);
    }

    @Benchmark
    @Threads(8)
    public void concurrentPut(WindowState state) {
        state.window.put(state.element);
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @Threads(8)
    public long diagnosticSequence(SequenceState state) {
        return state.sequence.getAndIncrement();
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @Threads(1)
    public ProbeEntry diagnosticAllocation(AllocationState state) {
        return new ProbeEntry(state.sequence++, state.element);
    }
}
