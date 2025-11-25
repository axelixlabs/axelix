import { EThreadDumpStateColors, EThreadState, type IThread } from "models";

export const threadStateColor = (singleHistory: IThread) => {
    if (singleHistory.threadState === EThreadState.RUNNABLE && singleHistory.inNative) {
        return EThreadDumpStateColors.BLUE;
    }

    if (singleHistory.threadState === EThreadState.RUNNABLE && !singleHistory.inNative) {
        return EThreadDumpStateColors.GREEN;
    }

    if (
        (singleHistory.threadState === EThreadState.WAITING && !singleHistory.suspended) ||
        singleHistory.threadState === EThreadState.TIMED_WAITING
    ) {
        return EThreadDumpStateColors.ORANGE;
    }

    if (singleHistory.threadState === EThreadState.BLOCKED) {
        return EThreadDumpStateColors.RED;
    }

    if (singleHistory.threadState === EThreadState.WAITING && singleHistory.suspended) {
        return EThreadDumpStateColors.YELLOW;
    }

    if (singleHistory.threadState === EThreadState.NEW) {
        return EThreadDumpStateColors.WHITE;
    }

    if (singleHistory.threadState === EThreadState.TERMINATED) {
        return EThreadDumpStateColors.GREY;
    }
};
