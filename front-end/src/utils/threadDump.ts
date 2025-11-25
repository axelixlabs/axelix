import { EThreadState } from "models";

export const threadDumpStateLetters: Record<EThreadState, string> = {
    [EThreadState.NEW]: "N",
    [EThreadState.RUNNABLE]: "R",
    [EThreadState.BLOCKED]: "B",
    [EThreadState.WAITING]: "W",
    [EThreadState.TIMED_WAITING]: "T",
    [EThreadState.TERMINATED]: "F",
};
