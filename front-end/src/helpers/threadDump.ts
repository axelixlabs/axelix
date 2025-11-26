import type { Dispatch, SetStateAction } from "react";

import { EThreadDumpStateColors, EThreadState, type IThread } from "models";
import { FIFTEEN_SECONDS, TEN_MINUTES_MILLISECDONDS } from "utils";

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

export const generateTimeSlots = (setter: Dispatch<SetStateAction<Date[]>>): void => {
    const now = new Date();

    // 15 seconds interval between time slots
    const stepMilliseconds = FIFTEEN_SECONDS;

    // 10 minutes ahead from now
    const endTime = new Date(now.getTime() + TEN_MINUTES_MILLISECDONDS);
    const slots: Date[] = [];

    let currentTime = new Date(now);

    // generate all time slots from current time to endTime with a 15-second interval
    while (currentTime <= endTime) {
        slots.push(new Date(currentTime));
        currentTime = new Date(currentTime.getTime() + stepMilliseconds);
    }

    setter(slots);
};
