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
import type { EThreadState } from "models/enums/threadDump";

interface IIdentifiable {
    /**
     * Name of the class of the object
     */
    className: string;

    /**
     * Identity hash code of the object
     */
    identityHashCode?: number;
}

interface IStackFrame {
    /**
     *  Name of the class to which this stack frame belongs
     */
    className: string;

    /**
     * Line number in the source code
     */
    lineNumber: number;

    /**
     * Name of the method that is currently executed
     */
    methodName: string;

    /**
     * True if this is a native method
     */
    nativeMethod: boolean;

    /**
     * The name of the java module where current class resides (if module system is used)
     */
    moduleName?: string;

    /**
     * Module version
     */
    moduleVersion?: string;

    /**
     * Class loader name
     */
    classLoaderName?: string;

    /**
     * Source file name
     */
    fileName?: string;
}

interface IMonitor extends IIdentifiable {
    /**
     * Depth of the stack at which the monitor was acquired
     */
    lockedStackDepth: number;

    /**
     * Stack frame where the monitor was acquired
     */
    lockedStackFrame: IStackFrame;
}

export interface IThread {
    /** Name of the thread */
    threadName: string;

    /**
     * Id of the thread
     */
    threadId: number;

    /**
     * The total time (in ms) the thread spent being blocked
     */
    blockedTime: number;

    /**
     * Number of times the thread was blocked
     */
    blockedCount: number;

    /**
     * The total time (in ms) the thread spent in WAITING/TIMED_WAITING state
     */
    waitedTime: number;

    /**
     * Number of times the thread was waiting
     */
    waitedCount: number;

    /**
     * Information about the lock the thread is waiting for
     */
    lockInfo?: IIdentifiable;

    /**
     * Name of the lock
     */
    lockName?: string;

    /**
     * Id of the thread currently owning the lock
     */
    lockOwnerId: number;

    /**
     * Name of the thread currently owning the lock
     */
    lockOwnerName?: string;

    /**
     * True if the thread is a daemon thread
     */
    daemon: boolean;

    /**
     * True if the thread is executing native code
     */
    inNative: boolean;

    /**
     * True if the thread is suspended
     */
    suspended: boolean;

    /**
     * Current state of the thread
     */
    threadState: EThreadState;

    /**
     * Thread priority
     */
    priority: number;

    /**
     * Stack trace of the thread
     */
    stackTrace: IStackFrame[];

    /**
     *  Monitors currently held by the thread
     */
    lockedMonitors: IMonitor[];

    /**
     * Synchronizers currently held by the thread
     */
    lockedSynchronizers: IIdentifiable[];
}

export interface IThreadDumpResponseBody {
    /**
     * Whether thread contention monitoring is enabled
     */
    threadContentionMonitoringEnabled: boolean;

    /**
     * Array of threads in the dump
     */
    threads: IThread[];
}

/**
 * The group of thread dump snapshots that logically represent a single state of the thread.
 * For example, if we have two snapshots of the thread dump - A and B, and in both cases the
 * thread was in WAITING state for the same monitor, then we can safely merge these two snapshots
 * together into a single logical "Thread Group".
 *
 * This concept is purely for visual purposes. It is not meant to represent any kind of thread group
 * inside the Java Virtual Machine. It is there just to provide the developer with information, that
 * for these N thread dump snapshots, thread 'X' was in the same state.
 */
export interface IThreadGroup {
    /**
     * Id of the thread group. Each thread group has a unique id, even across groups within single thread.
     */
    id: string;

    /**
     * Single thread dump, that represents the entire group.
     */
    thread: IThread;

    /**
     * Number of thread dumps snapshots combined in the group
     */
    count: number;
}
