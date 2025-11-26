import { EThreadState } from "models";

export const threadDumpStateLetters: Record<EThreadState, string> = {
    [EThreadState.NEW]: "N",
    [EThreadState.RUNNABLE]: "R",
    [EThreadState.BLOCKED]: "B",
    [EThreadState.WAITING]: "W",
    [EThreadState.TIMED_WAITING]: "T",
    [EThreadState.TERMINATED]: "F",
};

export const TEN_MINUTES_MILLISECDONDS = 10 * 60 * 1000;
export const FIFTEEN_SECONDS = 15 * 1000;
