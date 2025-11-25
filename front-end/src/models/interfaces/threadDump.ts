import type { EThreadState } from "models/enums/threadDump";

interface ILockInfo {
    className: string;
    identityHashCode: number;
}

interface ITrace {
    className: string;
    lineNumber: number;
    methodName: string;
    nativeMethod: boolean;
    moduleName?: string;
    moduleVersion?: string;
    classLoaderName?: string;
    fileName?: string;
}

// Verevin@ u varin@ nuyn obyektn e, opcionalov u banov sax

interface ILockedStackFrame {
    classLoaderName: string;
    className: string;
    fileName: string;
    lineNumber: number;
    methodName: string;
    moduleName: string;
    moduleVersion: string;
    nativeMethod: boolean;
}

interface ILockedMonitor {
    className: string;
    identityHashCode: number;
    lockedStackDepth: number;
    lockedStackFrame: ILockedStackFrame;
}

interface ILockedSynchronizer {
    className: string;
    identityHashCode: number;
}

export interface IThread {
    threadName: string;
    threadId: number;
    blockedTime: number;
    blockedCount: number;
    waitedTime: number;
    waitedCount: number;
    lockInfo?: ILockInfo;
    lockName?: string;
    lockOwnerId: number;
    lockOwnerName?: string;
    daemon: boolean;
    inNative: boolean;
    suspended: boolean;
    threadState: EThreadState;
    priority: number;
    stackTrace: ITrace[];
    lockedMonitors: ILockedMonitor[];
    lockedSynchronizers: ILockedSynchronizer[];
}

export interface IThreadDumpResponseBody {
    threads: IThread[];
}
